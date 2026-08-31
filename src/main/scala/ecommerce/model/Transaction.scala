package ecommerce.model

/** Represents a single e-commerce transaction record.
  *
  * This is an immutable data model. It carries no behavior beyond
  * simple data holding, keeping business logic in the analytics layer.
  *
  * @param transactionId unique identifier of the transaction
  * @param userId        identifier of the user who made the purchase
  * @param productId     identifier of the purchased product
  * @param category      product category at the time of purchase
  * @param price         unit price of the product
  * @param quantity      number of units purchased
  * @param country       country where the purchase was made
  * @param date          date of the transaction, as a raw string (e.g. "2026-01-01")
  */
final case class Transaction(
    transactionId: Long,
    userId: Long,
    productId: Long,
    category: String,
    price: Double,
    quantity: Int,
    country: String,
    date: String
)
