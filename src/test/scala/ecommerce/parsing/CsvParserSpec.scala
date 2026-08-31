package ecommerce.parsing

import ecommerce.model.{Product, Transaction}
import org.scalatest.funsuite.AnyFunSuite

/** Verifies that the concrete parsers can be used polymorphically through
  * the generic [[CsvParser]] trait, demonstrating trait-based abstraction
  * without requiring Spark.
  */
class CsvParserSpec extends AnyFunSuite {

  test("TransactionParser can be referenced as a CsvParser[Transaction]") {
    val parser: CsvParser[Transaction] = TransactionParser
    val result = parser.parseLine("1,10,100,Electronics,50.0,3,Israel,2026-01-01")

    assert(result.isRight)
  }

  test("ProductParser can be referenced as a CsvParser[Product]") {
    val parser: CsvParser[Product] = ProductParser
    val result = parser.parseLine("1,Wireless Headphones,Electronics")

    assert(result.isRight)
  }
}
