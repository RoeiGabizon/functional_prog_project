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

  // TODO: add tests for revenueByCategory, purchasesByCountry, topProducts,
  //       topCustomers and averageTransactionValue once implemented.
}
