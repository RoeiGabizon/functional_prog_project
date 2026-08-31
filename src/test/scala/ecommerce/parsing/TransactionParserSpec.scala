package ecommerce.parsing

import ecommerce.model.Transaction
import org.scalatest.funsuite.AnyFunSuite

class TransactionParserSpec extends AnyFunSuite {

  test("parseLine returns Right(Transaction) for a valid CSV line") {
    val line = "1,10,100,Electronics,50.0,3,Israel,2026-01-01"
    val expected = Transaction(
      transactionId = 1,
      userId = 10,
      productId = 100,
      category = "Electronics",
      price = 50.0,
      quantity = 3,
      country = "Israel",
      date = "2026-01-01"
    )

    assert(TransactionParser.parseLine(line) == Right(expected))
  }

  test("parseLine returns Left(error) for a line with the wrong number of fields") {
    val line = "1,10,100,Electronics"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when a numeric value cannot be parsed") {
    val line = "1,10,100,Electronics,not-a-price,3,Israel,2026-01-01"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when price is negative") {
    val line = "1,10,100,Electronics,-10.0,3,Israel,2026-01-01"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when quantity is zero") {
    val line = "1,10,100,Electronics,50.0,0,Israel,2026-01-01"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when quantity is negative") {
    val line = "1,10,100,Electronics,50.0,-3,Israel,2026-01-01"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when category is empty") {
    val line = "1,10,100,,50.0,3,Israel,2026-01-01"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when country is empty") {
    val line = "1,10,100,Electronics,50.0,3,,2026-01-01"

    assert(TransactionParser.parseLine(line).isLeft)
  }

  test("TransactionParser can be used through the CsvParser trait") {
    val parser: CsvParser[Transaction] = TransactionParser
    val line = "1,10,100,Electronics,50.0,3,Israel,2025-01-01"

    assert(parser.parseLine(line).isRight)
  }
}
