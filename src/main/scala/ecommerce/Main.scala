package ecommerce

import ecommerce.analytics.TransactionAnalytics
import ecommerce.io.{DataLoader, DataWriter}
import org.apache.spark.sql.SparkSession

/** Entry point of the Functional E-Commerce Analytics application.
  *
  * Main acts purely as the orchestration / I/O (imperative) shell:
  * it wires together the SparkSession, data loading, parsing, analytics
  * and data writing layers. It should stay small and free of business logic.
  *
  * The functional core (parsing, analytics, functional utilities) is kept
  * independent from Spark and I/O concerns wherever possible.
  */
object Main {

  private val TransactionsPath = "data/transactions.csv"
  private val RevenueByCategoryOutputPath = "output/revenue_by_category"
  private val PurchasesByCountryOutputPath = "output/purchases_by_country"
  private val ExpensiveTransactionsOutputPath = "output/expensive_transactions"
  private val ExpensiveTransactionThreshold = 50.0

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Functional E-Commerce Analytics")
      .master("local[*]")
      .getOrCreate()

    println("Spark session started successfully.")

    // 1. Load raw transaction lines (I/O layer).
    val rawLines = DataLoader.loadTransactionLines(spark, TransactionsPath)

    // 2 & 3. Parse lines into valid Transactions (pure parsing + Spark map/flatMap).
    val transactions = TransactionAnalytics.parseTransactions(rawLines)
    transactions.cache()

    println(s"Total valid transactions: ${transactions.count()}")

    // 4. Revenue by category.
    val revenueByCategory = TransactionAnalytics.revenueByCategory(transactions)

    // 5. Purchases by country.
    val purchasesByCountry = TransactionAnalytics.purchasesByCountry(transactions)

    // 6. Transactions with price >= threshold, using the curried predicate.
    val expensiveTransactions = transactions.filter(TransactionAnalytics.minimumPrice(ExpensiveTransactionThreshold))

    // 7. Persist results (I/O layer).
    DataWriter.writeLines(revenueByCategory.map { case (category, total) => s"$category,$total" }, RevenueByCategoryOutputPath)
    DataWriter.writeLines(purchasesByCountry.map { case (country, count) => s"$country,$count" }, PurchasesByCountryOutputPath)
    DataWriter.writeLines(expensiveTransactions.map(_.toString), ExpensiveTransactionsOutputPath)

    spark.stop()
    println("Spark session stopped successfully.")
  }
}
