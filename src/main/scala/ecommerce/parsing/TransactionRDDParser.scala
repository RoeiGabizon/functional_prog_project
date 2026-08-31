package ecommerce.parsing

import ecommerce.model.Transaction
import org.apache.spark.rdd.RDD

/** Spark-specific transformation from raw CSV lines into [[Transaction]]
  * values.
  *
  * This object bridges the pure, Spark-free [[TransactionParser]] with the
  * distributed world of Spark RDDs. It contains no parsing rules itself —
  * those live in [[TransactionParser]] — only the RDD transformations
  * (`map`, `flatMap`, `filter`) needed to apply that pure logic across a
  * distributed collection of lines and to summarize the outcome.
  */
object TransactionRDDParser {

  /** Parses every raw line into an `Either[String, Transaction]`, keeping
    * both successes and failures visible.
    *
    * This is the single source of truth for parsing results: other
    * functions in this object (`validTransactions`, `errors`) derive their
    * output from an already-computed `RDD[Either[String, Transaction]]`
    * rather than re-parsing the raw lines, so callers can `cache()` the
    * result of this function once and reuse it cheaply.
    *
    * @param lines raw, unparsed CSV lines (header already removed)
    * @return an RDD of parsing results, one per input line
    */
  def parseResults(lines: RDD[String]): RDD[Either[String, Transaction]] =
    lines.map(TransactionParser.parseLine)

  /** Extracts only the successfully parsed transactions from parsing results.
    *
    * Pattern matching is used inside `flatMap` to keep only the successful
    * results: `Right(transaction)` contributes exactly one element to the
    * resulting RDD, while `Left(_)` (a parsing failure) contributes none.
    * This is a standard functional idiom for turning a validation result
    * into an optional value without throwing exceptions or using mutable
    * state.
    *
    * @param results parsing results, typically produced by [[parseResults]]
    * @return an RDD containing only the successfully parsed transactions
    */
  def validTransactions(results: RDD[Either[String, Transaction]]): RDD[Transaction] =
    results.flatMap {
      case Right(transaction) => Some(transaction)
      case Left(_)            => None
    }

  /** Extracts only the error messages from parsing results.
    *
    * Symmetric to [[validTransactions]]: pattern matching inside `flatMap`
    * keeps `Left(message)` results and discards `Right(_)` results.
    *
    * @param results parsing results, typically produced by [[parseResults]]
    * @return an RDD containing only the parsing error messages
    */
  def errors(results: RDD[Either[String, Transaction]]): RDD[String] =
    results.flatMap {
      case Left(message) => Some(message)
      case Right(_)       => None
    }

  /** Convenience function that parses raw lines directly into valid
    * transactions, discarding any parsing failures.
    *
    * Useful when the caller has no need to inspect invalid records.
    *
    * @param lines raw, unparsed CSV lines (header already removed)
    * @return an RDD containing only the successfully parsed transactions
    */
  def parseTransactions(lines: RDD[String]): RDD[Transaction] =
    validTransactions(parseResults(lines))
}
