package ecommerce.parsing

import ecommerce.model.Product
import org.apache.spark.rdd.RDD

/** Spark-specific transformation from raw CSV lines into [[Product]] values.
  *
  * Mirrors [[TransactionRDDParser]]: it contains no parsing rules itself —
  * those live in [[ProductParser]] — only the RDD transformations (`map`,
  * `flatMap`) needed to apply that pure logic across a distributed
  * collection of lines and to summarize the outcome.
  */
object ProductRDDParser {

  /** Parses every raw line into an `Either[String, Product]`, keeping both
    * successes and failures visible.
    *
    * @param lines raw, unparsed CSV lines (header already removed)
    * @return an RDD of parsing results, one per input line
    */
  def parseResults(lines: RDD[String]): RDD[Either[String, Product]] =
    lines.map(ProductParser.parseLine)

  /** Extracts only the successfully parsed products from parsing results.
    *
    * Pattern matching is used inside `flatMap` to keep only the successful
    * results: `Right(product)` contributes exactly one element to the
    * resulting RDD, while `Left(_)` (a parsing failure) contributes none.
    *
    * @param results parsing results, typically produced by [[parseResults]]
    * @return an RDD containing only the successfully parsed products
    */
  def validProducts(results: RDD[Either[String, Product]]): RDD[Product] =
    results.flatMap {
      case Right(product) => Some(product)
      case Left(_)         => None
    }

  /** Extracts only the error messages from parsing results.
    *
    * @param results parsing results, typically produced by [[parseResults]]
    * @return an RDD containing only the parsing error messages
    */
  def errors(results: RDD[Either[String, Product]]): RDD[String] =
    results.flatMap {
      case Left(message) => Some(message)
      case Right(_)       => None
    }

  /** Convenience function that parses raw lines directly into valid
    * products, discarding any parsing failures.
    *
    * @param lines raw, unparsed CSV lines (header already removed)
    * @return an RDD containing only the successfully parsed products
    */
  def parseProducts(lines: RDD[String]): RDD[Product] =
    validProducts(parseResults(lines))
}
