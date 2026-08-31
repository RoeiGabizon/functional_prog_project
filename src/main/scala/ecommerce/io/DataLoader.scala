package ecommerce.io

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/** Responsible ONLY for reading external data into Spark.
  *
  * DataLoader must not contain any parsing, validation or analytics logic.
  * Its sole responsibility is to know where the raw data lives and how
  * to bring it into Spark as an RDD, leaving interpretation of the
  * content (parsing, validation) to the parsing layer and computation
  * to the analytics layer.
  */
object DataLoader {

  /** Loads a text file as an RDD of raw lines, stripping the CSV header.
    *
    * This is the single, generic entry point for reading any of our
    * CSV files. It performs no parsing: each element of the resulting
    * RDD is still a raw, unparsed comma-separated line.
    *
    * @param spark the active SparkSession, used to obtain the SparkContext
    * @param path  path to the CSV file to load
    * @return an RDD of raw CSV lines, without the header row
    */
  def loadLines(spark: SparkSession, path: String): RDD[String] = {
    val allLines = spark.sparkContext.textFile(path)
    val header = allLines.first()
    allLines.filter(line => line != header)
  }

  /** Loads the raw transactions CSV file as an RDD of unparsed lines.
    *
    * @param spark            the active SparkSession
    * @param transactionsPath path to the transactions CSV file
    * @return an RDD containing the raw, unparsed transaction lines
    */
  def loadTransactionLines(spark: SparkSession, transactionsPath: String): RDD[String] =
    loadLines(spark, transactionsPath)

  /** Loads the raw products CSV file as an RDD of unparsed lines.
    *
    * @param spark        the active SparkSession
    * @param productsPath path to the products CSV file
    * @return an RDD containing the raw, unparsed product lines
    */
  def loadProductLines(spark: SparkSession, productsPath: String): RDD[String] =
    loadLines(spark, productsPath)
}
