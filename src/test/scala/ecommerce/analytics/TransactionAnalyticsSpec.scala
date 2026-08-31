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

  test("belongsToCategory(\"Electronics\") returns true for an Electronics transaction") {
    val transaction = Transaction(1, 10, 100, "Electronics", 50.0, 1, "Israel", "2026-01-01")

    assert(TransactionAnalytics.belongsToCategory("Electronics")(transaction))
  }

  test("belongsToCategory(\"Electronics\") returns false for a different category") {
    val transaction = Transaction(1, 10, 100, "Books", 50.0, 1, "Israel", "2026-01-01")

    assert(!TransactionAnalytics.belongsToCategory("Electronics")(transaction))
  }

  test("minimumQuantity(2) returns true when quantity meets the threshold") {
    val transaction = Transaction(1, 10, 100, "Electronics", 50.0, 2, "Israel", "2026-01-01")

    assert(TransactionAnalytics.minimumQuantity(2)(transaction))
  }

  test("minimumQuantity(2) returns false when quantity is below the threshold") {
    val transaction = Transaction(1, 10, 100, "Electronics", 50.0, 1, "Israel", "2026-01-01")

    assert(!TransactionAnalytics.minimumQuantity(2)(transaction))
  }

  test("sumRevenue sums a list of revenue values") {
    assert(TransactionAnalytics.sumRevenue(List(10.0, 20.0, 30.0)) == 60.0)
  }

  test("sumRevenue returns 0.0 for an empty list") {
    assert(TransactionAnalytics.sumRevenue(Nil) == 0.0)
  }

  // TODO: add tests for topProducts, topCustomers and averageTransactionValue once implemented.
}
