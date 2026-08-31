package ecommerce.generator

import java.io.{BufferedWriter, FileWriter}
import scala.util.Random

/** Development-only tool that generates sample `products.csv` and
  * `transactions.csv` files for local testing and coursework demonstration.
  *
  * This object is tooling/I/O code, not part of the analytics core: it uses
  * `scala.util.Random` to fabricate realistic-looking data, which is
  * inherently non-deterministic in general, but reproducible here because a
  * fixed seed (42) is used. Running this generator again with the same seed
  * and settings produces byte-identical output.
  *
  * This is a manually-run executable object; `Main` never invokes it and
  * only consumes the CSV files it produces.
  */
object DataGenerator {

  private val Seed = 42
  private val ProductCount = 80
  private val TransactionCount = 15000

  // Small, controlled fraction of intentionally invalid transaction rows,
  // so the parsing/validation pipeline has something meaningful to report.
  private val InvalidRowFraction = 0.007

  private val Categories = Seq("Electronics", "Books", "Gaming", "Clothing", "Home", "Sports", "Beauty", "Toys")
  private val Countries = Seq("Israel", "USA", "UK", "Germany", "France", "Canada", "Japan", "Australia")

  private val ProductNamesByCategory: Map[String, Seq[String]] = Map(
    "Electronics" -> Seq("Wireless Headphones", "Smart Watch", "Bluetooth Speaker", "4K Monitor", "Power Bank"),
    "Books" -> Seq("Functional Programming in Scala", "Clean Code", "Science Fiction Anthology", "History of Rome", "Cookbook"),
    "Gaming" -> Seq("Wireless Controller", "Gaming Mouse", "Mechanical Keyboard", "VR Headset", "Gaming Chair"),
    "Clothing" -> Seq("Cotton T-Shirt", "Denim Jeans", "Running Shoes", "Winter Jacket", "Wool Socks"),
    "Home" -> Seq("Coffee Maker", "Vacuum Cleaner", "Desk Lamp", "Bed Sheets", "Kitchen Knife Set"),
    "Sports" -> Seq("Yoga Mat", "Dumbbell Set", "Tennis Racket", "Cycling Helmet", "Football"),
    "Beauty" -> Seq("Face Moisturizer", "Shampoo", "Perfume", "Lipstick", "Sunscreen"),
    "Toys" -> Seq("Building Blocks Set", "Puzzle 1000 Pieces", "Remote Control Car", "Board Game", "Action Figure")
  )

  /** Generates `data/products.csv` and `data/transactions.csv`.
    *
    * Run manually (e.g. from IntelliJ or `sbt "runMain ecommerce.generator.DataGenerator"`)
    * whenever a fresh sample dataset is needed. This is intentionally not
    * invoked by `Main`.
    */
  def main(args: Array[String]): Unit = {
    val random = new Random(Seed)

    val products = generateProducts(random)
    writeCsv("data/products.csv", "productId,name,category", products.map(productToCsv))
    println(s"Generated ${products.size} products to data/products.csv")

    val transactions = generateTransactionLines(random, products)
    writeCsv(
      "data/transactions.csv",
      "transactionId,userId,productId,category,price,quantity,country,date",
      transactions
    )
    println(s"Generated ${transactions.size} transactions to data/transactions.csv")
  }

  /** A generated product record, paired with its price range so that
    * transactions referencing it can generate a plausible price.
    */
  private final case class GeneratedProduct(productId: Long, name: String, category: String)

  private def generateProducts(random: Random): Seq[GeneratedProduct] =
    (1 to ProductCount).map { productId =>
      val category = Categories(random.nextInt(Categories.size))
      val namesForCategory = ProductNamesByCategory(category)
      val baseName = namesForCategory(random.nextInt(namesForCategory.size))
      val name = s"$baseName #$productId"
      GeneratedProduct(productId.toLong, name, category)
    }

  private def productToCsv(product: GeneratedProduct): String =
    s"${product.productId},${product.name},${product.category}"

  private def generateTransactionLines(random: Random, products: Seq[GeneratedProduct]): Seq[String] =
    (1 to TransactionCount).map { transactionId =>
      val isInvalid = random.nextDouble() < InvalidRowFraction
      if (isInvalid) invalidTransactionLine(random, transactionId, products)
      else validTransactionLine(random, transactionId, products)
    }

  private def validTransactionLine(random: Random, transactionId: Int, products: Seq[GeneratedProduct]): String = {
    val product = products(random.nextInt(products.size))
    val userId = 1 + random.nextInt(2000)
    val price = roundToCents(1.0 + random.nextDouble() * 499.0)
    val quantity = 1 + random.nextInt(5)
    val country = Countries(random.nextInt(Countries.size))
    val date = randomDate(random)

    s"$transactionId,$userId,${product.productId},${product.category},$price,$quantity,$country,$date"
  }

  /** Produces a deliberately invalid transaction line, using one of a few
    * simple, clearly documented invalidation strategies (negative price,
    * zero quantity, or a non-numeric field), so the resulting file is
    * still mostly well-formed but contains a small, known fraction of
    * rows that the parsing/validation pipeline is expected to reject.
    */
  private def invalidTransactionLine(random: Random, transactionId: Int, products: Seq[GeneratedProduct]): String = {
    val product = products(random.nextInt(products.size))
    val userId = 1 + random.nextInt(2000)
    val quantity = 1 + random.nextInt(5)
    val country = Countries(random.nextInt(Countries.size))
    val date = randomDate(random)

    random.nextInt(3) match {
      case 0 => // negative price
        val price = roundToCents(-(1.0 + random.nextDouble() * 100.0))
        s"$transactionId,$userId,${product.productId},${product.category},$price,$quantity,$country,$date"
      case 1 => // zero quantity
        val price = roundToCents(1.0 + random.nextDouble() * 100.0)
        s"$transactionId,$userId,${product.productId},${product.category},$price,0,$country,$date"
      case _ => // non-numeric price
        s"$transactionId,$userId,${product.productId},${product.category},not-a-price,$quantity,$country,$date"
    }
  }

  private def roundToCents(value: Double): Double =
    math.round(value * 100.0) / 100.0

  private def randomDate(random: Random): String = {
    val month = 1 + random.nextInt(12)
    val day = 1 + random.nextInt(28)
    f"2026-$month%02d-$day%02d"
  }

  private def writeCsv(path: String, header: String, lines: Seq[String]): Unit = {
    val writer = new BufferedWriter(new FileWriter(path))
    try {
      writer.write(header)
      writer.newLine()
      lines.foreach { line =>
        writer.write(line)
        writer.newLine()
      }
    } finally {
      writer.close()
    }
  }
}
