package ecommerce.model

/** Represents a product catalog entry.
  *
  * This is an immutable data model used to enrich or cross-reference
  * transaction data.
  *
  * @param productId unique identifier of the product
  * @param name      display name of the product
  * @param category  category the product belongs to
  */
final case class Product(
    productId: Long,
    name: String,
    category: String
)
