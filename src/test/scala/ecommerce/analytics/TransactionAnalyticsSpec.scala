package ecommerce.analytics

import ecommerce.model.Transaction
import org.scalatest.funsuite.AnyFunSuite

class TransactionAnalyticsSpec extends AnyFunSuite {

  test("revenue is price multiplied by quantity") {
    val transaction = Transaction(
      transactionId = 1,
      userId = 10,
      productId = 100,
      category = "Electronics",
      price = 50.0,
      quantity = 3,
      country = "Israel",
      date = "2026-01-01"
    )

    assert(TransactionAnalytics.revenue(transaction) == 150.0)
  }

  test("minimumPrice(50.0) returns true when price is exactly the threshold") {
    val transaction = Transaction(1, 10, 100, "Electronics", 50.0, 1, "Israel", "2026-01-01")

    assert(TransactionAnalytics.minimumPrice(50.0)(transaction))
  }

  test("minimumPrice(50.0) returns true when price is above the threshold") {
    val transaction = Transaction(1, 10, 100, "Electronics", 120.0, 1, "Israel", "2026-01-01")

    assert(TransactionAnalytics.minimumPrice(50.0)(transaction))
  }

  test("minimumPrice(50.0) returns false when price is below the threshold") {
    val transaction = Transaction(1, 10, 100, "Books", 15.5, 2, "USA", "2026-01-02")

    assert(!TransactionAnalytics.minimumPrice(50.0)(transaction))
  }

  // TODO: add tests for topProducts, topCustomers and averageTransactionValue once implemented.
}
