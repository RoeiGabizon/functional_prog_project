package ecommerce.parsing

import ecommerce.model.Transaction

import scala.util.Try

/** Pure parsing logic for turning raw CSV lines into [[Transaction]] values.
  *
  * This object contains no I/O and no Spark dependency: it operates purely
  * on Strings, making it easy to unit test in isolation. Parsing failures
  * are represented functionally via `Either`, rather than thrown exceptions.
  */
object TransactionParser {

  private val ExpectedFieldCount = 8

  /** Parses a single CSV line into a [[Transaction]].
    *
    * Expected column order:
    * transactionId,userId,productId,category,price,quantity,country,date
    *
    * @param line a raw, comma-separated CSV line (without header)
    * @return `Right(Transaction)` on success, or `Left(errorMessage)` on failure
    */
  def parseLine(line: String): Either[String, Transaction] = {
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
      } yield Transaction(transactionId, userId, productId, category, price, quantity, country, date)
    }
  }

  private def parseLong(value: String, fieldName: String): Either[String, Long] =
    Try(value.toLong).toEither.left.map(_ => s"Invalid $fieldName: '$value'")

  private def parseInt(value: String, fieldName: String): Either[String, Int] =
    Try(value.toInt).toEither.left.map(_ => s"Invalid $fieldName: '$value'")

  private def parseDouble(value: String, fieldName: String): Either[String, Double] =
    Try(value.toDouble).toEither.left.map(_ => s"Invalid $fieldName: '$value'")
}
