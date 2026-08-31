package ecommerce.parsing

import ecommerce.functional.FunctionalUtils
import ecommerce.model.Transaction

import scala.util.Try

/** Pure parsing logic for turning raw CSV lines into [[Transaction]] values.
  *
  * This object contains no I/O and no Spark dependency: it operates purely
  * on Strings, making it easy to unit test in isolation. Parsing failures
  * are represented functionally via `Either`, rather than thrown exceptions.
  * Implements [[CsvParser]] for [[Transaction]].
  */
object TransactionParser extends CsvParser[Transaction] {

  private val ExpectedFieldCount = 8

  /** Parses a single CSV line into a [[Transaction]].
    *
    * Expected column order:
    * transactionId,userId,productId,category,price,quantity,country,date
    *
    * @param line a raw, comma-separated CSV line (without header)
    * @return `Right(Transaction)` on success, or `Left(errorMessage)` on failure
    */
  override def parseLine(line: String): Either[String, Transaction] = {
    val fields = line.split(",", -1).map(_.trim)

    if (fields.length != ExpectedFieldCount) {
      Left(s"Expected $ExpectedFieldCount fields but found ${fields.length}: '$line'")
    } else {
      val Array(transactionIdStr, userIdStr, productIdStr, category, priceStr, quantityStr, country, date) = fields

      for {
        transactionId <- parseLong(transactionIdStr, "transactionId")
        userId        <- parseLong(userIdStr, "userId")
        productId     <- parseLong(productIdStr, "productId")
        price         <- parseDouble(priceStr, "price")
        quantity      <- parseInt(quantityStr, "quantity")
        // Function composition: normalize raw category text (trim, lower-case,
        // capitalize) before validating/storing it, so equivalent inputs such
        // as " Electronics" and "ELECTRONICS " end up as the same "Electronics".
        validCategory <- requireNonEmpty(FunctionalUtils.normalizeCategory(category), "category")
        validCountry  <- requireNonEmpty(country, "country")
        validPrice    <- requireNonNegativePrice(price)
        validQuantity <- requirePositiveQuantity(quantity)
      } yield Transaction(transactionId, userId, productId, validCategory, validPrice, validQuantity, validCountry, date)
    }
  }

  private def parseLong(value: String, fieldName: String): Either[String, Long] =
    Try(value.toLong).toEither.left.map(_ => s"Invalid $fieldName: '$value'")

  private def parseInt(value: String, fieldName: String): Either[String, Int] =
    Try(value.toInt).toEither.left.map(_ => s"Invalid $fieldName: '$value'")

  private def parseDouble(value: String, fieldName: String): Either[String, Double] =
    Try(value.toDouble).toEither.left.map(_ => s"Invalid $fieldName: '$value'")

  private def requireNonEmpty(value: String, fieldName: String): Either[String, String] =
    if (value.isEmpty) Left(s"Field $fieldName must not be empty") else Right(value)

  private def requireNonNegativePrice(price: Double): Either[String, Double] =
    if (price < 0) Left(s"Price must not be negative: $price") else Right(price)

  private def requirePositiveQuantity(quantity: Int): Either[String, Int] =
    if (quantity <= 0) Left(s"Quantity must be positive: $quantity") else Right(quantity)
}
