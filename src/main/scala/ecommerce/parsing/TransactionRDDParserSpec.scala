package ecommerce.parsing

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

/** Lightweight Spark integration tests for [[TransactionRDDParser]].
  *
  * A single local SparkSession is shared across all tests in this spec to
  * keep the suite fast, since starting Spark has meaningful overhead.
  */
class TransactionRDDParserSpec extends AnyFunSuite with BeforeAndAfterAll {

  private var spark: SparkSession = _

  override def beforeAll(): Unit = {
    val conf = new SparkConf()
      .setAppName("TransactionRDDParserSpec")
      .setMaster("local[1]")
    spark = SparkSession.builder().config(conf).getOrCreate()
  }

  override def afterAll(): Unit = {
    if (spark != null) spark.stop()
  }

  private val sampleLines = Seq(
    "1,10,100,Electronics,50.0,3,Israel,2026-01-01", // valid
    "2,11,101,Books,-5.0,2,USA,2026-01-02",           // invalid: negative price
    "3,12,102,Clothing,29.99,1,Germany,2026-01-02"    // valid
  )

  test("parseResults preserves one Either result per input line") {
    val lines = spark.sparkContext.parallelize(sampleLines)
    val results = TransactionRDDParser.parseResults(lines)

    assert(results.count() == sampleLines.size)
  }

  test("validTransactions keeps only successfully parsed transactions") {
    val lines = spark.sparkContext.parallelize(sampleLines)
    val results = TransactionRDDParser.parseResults(lines)

    assert(TransactionRDDParser.validTransactions(results).count() == 2)
  }

  test("errors keeps only the failed parsing messages") {
    val lines = spark.sparkContext.parallelize(sampleLines)
    val results = TransactionRDDParser.parseResults(lines)

    assert(TransactionRDDParser.errors(results).count() == 1)
  }
}
