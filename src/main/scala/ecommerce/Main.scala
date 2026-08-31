package ecommerce

import ecommerce.analytics.{ProductAnalytics, TransactionAnalytics}
import ecommerce.io.{DataLoader, DataWriter}
import ecommerce.parsing.{ProductRDDParser, TransactionRDDParser}
import org.apache.spark.sql.SparkSession

/** Entry point of the Functional E-Commerce Analytics application.
  *
  * Main acts purely as the orchestration / I/O (imperative) shell:
  * it wires together the SparkSession, data loading, parsing, analytics
  * and data writing layers. It should stay small and free of business logic.
  *
  * The functional core (parsing, analytics, functional utilities) is kept
  * independent from Spark and I/O concerns wherever possible.
  *
  * This object only consumes `data/transactions.csv` and `data/products.csv`;
  * it never generates them. Use [[ecommerce.generator.DataGenerator]]
  * separately to (re)create a sample dataset.
  */
object Main {

  private val TransactionsPath = "data/transactions.csv"
  private val ProductsPath = "data/products.csv"

  private val RevenueByCategoryOutputPath = "output/revenue_by_category"
  private val PurchasesByCountryOutputPath = "output/purchases_by_country"
  private val RevenueByProductOutputPath = "output/revenue_by_product"
  private val RevenueByProductCategoryOutputPath = "output/revenue_by_product_category"
  private val QuantityByProductOutputPath = "output/quantity_by_product"
  private val ExpensiveTransactionsOutputPath = "output/expensive_transactions"

  private val TopProductsLimit = 5
  private val ExpensiveTransactionThreshold = 300.0

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Functional E-Commerce Analytics")
      .master("local[*]")
      .getOrCreate()

    println("Spark session started successfully.")

    // 1 & 2. Load raw lines for both datasets (I/O layer).
    val rawTransactionLines = DataLoader.loadTransactionLines(spark, TransactionsPath)
    val rawProductLines = DataLoader.loadProductLines(spark, ProductsPath)

    // 3. Parse and validate both datasets.
    // Cached because each parse-result RDD is the shared source for both its
    // validation counts and its derived valid-record RDD below.
    val transactionParseResults = TransactionRDDParser.parseResults(rawTransactionLines).cache()
    val productParseResults = ProductRDDParser.parseResults(rawProductLines).cache()

    val transactions = TransactionRDDParser.validTransactions(transactionParseResults).cache()
    val products = ProductRDDParser.validProducts(productParseResults).cache()

    // 4. Display validation counts.
    val transactionInputCount = transactionParseResults.count()
    val validTransactionCount = transactions.count()
    println("Transactions:")
    println(s"Input records: $transactionInputCount")
    println(s"Valid transactions: $validTransactionCount")
    println(s"Invalid transactions: ${transactionInputCount - validTransactionCount}")

    val productInputCount = productParseResults.count()
    val validProductCount = products.count()
    println("Products:")
    println(s"Input records: $productInputCount")
    println(s"Valid products: $validProductCount")
    println(s"Invalid products: ${productInputCount - validProductCount}")

    // 5 & 6. transactions and products RDDs are ready above; 7. run analytics.
    val revenueByCategory = TransactionAnalytics.revenueByCategory(transactions)
    val purchasesByCountry = TransactionAnalytics.purchasesByCountry(transactions)
    val revenueByProduct = ProductAnalytics.revenueByProduct(transactions, products)
    val revenueByProductCategory = ProductAnalytics.revenueByProductCategory(transactions, products)
    val quantityByProduct = ProductAnalytics.quantitySoldByProduct(transactions, products)
    val topProducts = ProductAnalytics.topProductsByRevenue(transactions, products, TopProductsLimit)
    val missingProductReferences = ProductAnalytics.transactionsWithMissingProducts(transactions, products)

    // Curried predicate, partially applied to a fixed threshold: demonstrates
    // currying, a closure over `ExpensiveTransactionThreshold`, and Spark's filter.
    val expensiveTransactions = transactions.filter(TransactionAnalytics.minimumPrice(ExpensiveTransactionThreshold))

    println(s"Transactions referencing missing products: ${missingProductReferences.count()}")

    // 8. Save results (I/O layer).
    DataWriter.writeLines(revenueByCategory.map { case (category, total) => s"$category,$total" }, RevenueByCategoryOutputPath)
    DataWriter.writeLines(purchasesByCountry.map { case (country, count) => s"$country,$count" }, PurchasesByCountryOutputPath)
    DataWriter.writeLines(revenueByProduct.map { case (name, total) => s"$name,$total" }, RevenueByProductOutputPath)
    DataWriter.writeLines(revenueByProductCategory.map { case (category, total) => s"$category,$total" }, RevenueByProductCategoryOutputPath)
    DataWriter.writeLines(quantityByProduct.map { case (name, quantity) => s"$name,$quantity" }, QuantityByProductOutputPath)
    DataWriter.writeLines(
      expensiveTransactions.map { transaction =>
        import transaction._
        s"$transactionId,$userId,$productId,$category,$price,$quantity,$country,$date"
      },
      ExpensiveTransactionsOutputPath
    )

    // 9. Print a small summary of the top products.
    println(s"Top $TopProductsLimit products by revenue:")
    topProducts.foreach { case (name, total) => println(s"$name -> $total") }

    spark.stop()
    println("Spark session stopped successfully.")
  }
}
