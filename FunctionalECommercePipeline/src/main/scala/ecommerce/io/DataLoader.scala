package ecommerce.io

import org.apache.spark.sql.{DataFrame, SparkSession}

/** Responsible ONLY for reading external data into Spark.
  *
  * DataLoader must not contain any parsing, validation or analytics logic.
  * Its sole responsibility is to know where the raw data lives and how
  * to bring it into Spark as a DataFrame / RDD, leaving interpretation
  * of the content to the parsing layer.
  */
object DataLoader {

  /** Loads the raw transactions CSV file as a DataFrame.
    *
    * @param spark           the active SparkSession
    * @param transactionsPath path to the transactions CSV file
    * @return a DataFrame containing the raw, unparsed transaction rows
    */
  def loadTransactions(spark: SparkSession, transactionsPath: String): DataFrame = {
    // TODO: implement actual CSV reading (e.g. spark.read.option("header", "true").csv(transactionsPath))
    spark.emptyDataFrame
  }

  /** Loads the raw products CSV file as a DataFrame.
    *
    * @param spark       the active SparkSession
    * @param productsPath path to the products CSV file
    * @return a DataFrame containing the raw, unparsed product rows
    */
  def loadProducts(spark: SparkSession, productsPath: String): DataFrame = {
    // TODO: implement actual CSV reading (e.g. spark.read.option("header", "true").csv(productsPath))
    spark.emptyDataFrame
  }
}
