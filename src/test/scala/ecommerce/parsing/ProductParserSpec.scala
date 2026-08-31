package ecommerce.parsing

import ecommerce.model.Product
import org.scalatest.funsuite.AnyFunSuite

class ProductParserSpec extends AnyFunSuite {

  test("parseLine returns Right(Product) for a valid CSV line") {
    val line = "100,Wireless Headphones,Electronics"
    val expected = Product(productId = 100, name = "Wireless Headphones", category = "Electronics")

    assert(ProductParser.parseLine(line) == Right(expected))
  }

  test("parseLine returns Left(error) when productId cannot be parsed") {
    val line = "not-a-number,Wireless Headphones,Electronics"

    assert(ProductParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when productId is not positive") {
    val line = "0,Wireless Headphones,Electronics"

    assert(ProductParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when name is empty") {
    val line = "100,,Electronics"

    assert(ProductParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) when category is empty") {
    val line = "100,Wireless Headphones,"

    assert(ProductParser.parseLine(line).isLeft)
  }

  test("parseLine returns Left(error) for a line with the wrong number of fields") {
    val line = "100,Wireless Headphones"

    assert(ProductParser.parseLine(line).isLeft)
  }
}
