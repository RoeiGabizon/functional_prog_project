package ecommerce.parsing

import ecommerce.model.Product

import scala.util.Try

/** Pure parsing logic for turning raw CSV lines into [[Product]] values.
  *
  * This object contains no I/O and no Spark dependency: it operates purely
  * on Strings, making it easy to unit test in isolation. Parsing failures
  * are represented functionally via `Either`, rather than thrown exceptions.
  * Implements [[CsvParser]] for [[Product]].
  */
object ProductParser extends CsvParser[Product] {

  private val ExpectedFieldCount = 3

  /** Parses a single CSV line into a [[Product]].
    *
    * Expected column order:
    * productId,name,category
    *
    * @param line a raw, comma-separated CSV line (without header)
    * @return `Right(Product)` on success, or `Left(errorMessage)` on failure
    */
  override def parseLine(line: String): Either[String, Product] = {
    val fields = line.split(",", -1).map(_.trim)

    if (fields.length != ExpectedFieldCount) {
      Left(s"Expected $ExpectedFieldCount fields but found ${fields.length}: '$line'")
    } else {
      val Array(productIdStr, name, category) = fields

      for {
        productId     <- parseLong(productIdStr, "productId")
        validProductId <- requirePositive(productId)
        validName     <- requireNonEmpty(name, "name")
        validCategory <- requireNonEmpty(category, "category")
      } yield Product(validProductId, validName, validCategory)
    }
  }

  private def parseLong(value: String, fieldName: String): Either[String, Long] =
    Try(value.toLong).toEither.left.map(_ => s"Invalid $fieldName: '$value'")

  private def requirePositive(productId: Long): Either[String, Long] =
    if (productId <= 0) Left(s"productId must be positive: $productId") else Right(productId)

  private def requireNonEmpty(value: String, fieldName: String): Either[String, String] =
    if (value.isEmpty) Left(s"Field $fieldName must not be empty") else Right(value)
}
