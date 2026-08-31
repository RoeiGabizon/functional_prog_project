package ecommerce.io

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.rdd.RDD

/** Responsible ONLY for writing Spark computation results to storage.
  *
  * DataWriter must not contain any analytics or transformation logic.
  * It simply persists already-computed results (produced by the
  * analytics layer) into the output directory.
  */
object DataWriter {

  /** Saves an RDD of text lines to the given output path using Spark's
    * `saveAsTextFile`.
    *
    * Since `saveAsTextFile` fails if the target directory already exists,
    * this function first removes any pre-existing directory at
    * `outputPath` using the Hadoop FileSystem API, so that development
    * reruns work cleanly.
    *
    * @param data       the RDD of lines to persist (already formatted as strings)
    * @param outputPath destination directory for the output
    */
  def writeLines(data: RDD[String], outputPath: String): Unit = {
    deleteIfExists(data, outputPath)
    data.saveAsTextFile(outputPath)
  }

  /** Deletes the target output directory if it already exists, so that
    * `saveAsTextFile` can recreate it without failing.
    *
    * @param data       an RDD used only to access the SparkContext's Hadoop configuration
    * @param outputPath directory to remove, if present
    */
  private def deleteIfExists(data: RDD[String], outputPath: String): Unit = {
    val path = new Path(outputPath)
    val fileSystem = path.getFileSystem(data.sparkContext.hadoopConfiguration)
    if (fileSystem.exists(path)) {
      fileSystem.delete(path, true)
    }
  }
}
