package ecommerce

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

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Functional E-Commerce Analytics")
      .master("local[*]")
      .getOrCreate()

    println("Spark session started successfully.")

    // TODO: orchestrate the pipeline here once implemented:
    //   1. DataLoader.loadTransactions(spark, path)
    //   2. TransactionParser.parseLine(rawLine)
    //   3. TransactionAnalytics.* pure computations
    //   4. DataWriter.write(...) results to the output directory

    spark.stop()
    println("Spark session stopped successfully.")
  }
}
