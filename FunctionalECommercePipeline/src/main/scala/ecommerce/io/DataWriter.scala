package ecommerce.io

import org.apache.spark.sql.DataFrame

/** Responsible ONLY for writing Spark computation results to storage.
  *
  * DataWriter must not contain any analytics or transformation logic.
  * It simply persists already-computed results (produced by the
  * analytics layer) into the output directory.
  */
object DataWriter {

  /** Writes a DataFrame of results to the given output path.
    *
    * @param data       the computed result DataFrame to persist
    * @param outputPath destination directory for the output
    * @param format     output format (e.g. "csv", "parquet"), defaults to "csv"
    */
  def write(data: DataFrame, outputPath: String, format: String = "csv"): Unit = {
    // TODO: implement actual writing, e.g.:
    // data.write.mode("overwrite").option("header", "true").format(format).save(outputPath)
    ()
  }
}
