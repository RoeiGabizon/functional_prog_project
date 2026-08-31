# Functional Programming Techniques — FunctionalECommercePipeline

This is developer-facing documentation tracking where each required functional
programming (FP) technique is implemented in the codebase. It is **not** the
final university report/PDF; it exists to make writing that report easier
later, and it is kept in sync with the actual source code.

## Basic FP Techniques

| Technique | Class/Object | Function | Explanation |
|---|---|---|---|
| Immutable case classes | `ecommerce.model.Transaction`, `ecommerce.model.Product` | (case class fields) | All fields are `val`; instances cannot be mutated after creation. |
| Pure functions | `TransactionAnalytics` | `revenue` | Depends only on its input `Transaction`, has no side effects, and always returns the same output for the same input. |
| Immutable state | Project-wide | `val` everywhere, no `var` | All local bindings and RDDs are immutable; transformations produce new values/RDDs instead of mutating existing ones. |
| Higher-order functions | `ecommerce.functional.FunctionalUtils` | `transformIf` | Accepts two functions (`predicate`, `transformation`) as parameters and applies one of them depending on the predicate's result; demonstrated and verified by unit tests. |
| Higher-order functions (Spark) | Various | `map`, `flatMap`, `filter`, `reduceByKey` | Spark's RDD API itself is built on higher-order functions passed by the caller. |
| Currying | `TransactionAnalytics` | `minimumPrice`, `belongsToCategory`, `minimumQuantity` | Each has two parameter lists; partially applying the first (e.g. `minimumPrice(300.0)`) yields a reusable `Transaction => Boolean`. |
| Closures | `Main` | `expensiveTransactions`, `expensiveElectronics` | The predicates returned by the curried functions above close over the threshold/category values supplied at call time. |
| Trait / generic abstraction | `ecommerce.parsing.CsvParser` | `CsvParser[A]`, `TransactionParser`, `ProductParser` | `CsvParser[A]` defines a reusable parsing contract returning `Either[String, A]`; `TransactionParser` and `ProductParser` implement that contract for their respective immutable case classes. |

## Advanced FP Techniques

| Technique | Class/Object | Function | Explanation |
|---|---|---|---|
| Custom combinator | `ecommerce.functional.FunctionalUtils` | `and`, `or`, `not` | Each accepts one or two predicate functions and returns a brand-new composed predicate function — a combinator, distinct from `Function1.andThen`/`compose`. |
| Custom combinator usage | `Main` | `expensiveElectronics` | Combines `TransactionAnalytics.minimumPrice(300.0)` and `TransactionAnalytics.belongsToCategory("Electronics")` via `FunctionalUtils.and`, then passes the result to Spark's `filter`. |
| Function composition | `ecommerce.functional.FunctionalUtils` | `normalizeCategory` (= `trimText andThen lowerCaseText andThen capitalizeText`) | Combines three small pure `String => String` functions using `Function1.andThen`. |
| Function composition usage | `ecommerce.parsing.TransactionParser` | `parseLine` | Applies `normalizeCategory` to the raw category field before validating and building the `Transaction`. |
| Tail recursion | `ecommerce.analytics.TransactionAnalytics` | `sumRevenue` (private `@tailrec` helper `loop`) | Sums a small local `List[Double]` (e.g. the Top 5 product revenues) using an accumulator-based recursive call that is the last operation in the function, verified by `@tailrec`. |
| Pattern matching | `ecommerce.parsing.TransactionRDDParser`, `ecommerce.parsing.ProductRDDParser` | `validTransactions`, `errors`, `validProducts` | Match on `Right(value)` / `Left(message)` inside `flatMap` to separate successful parses from failures without exceptions. |
| Pattern matching | `ecommerce.analytics.ProductAnalytics` | `transactionsWithMissingProducts` | Matches on `Some(_)` / `None` after a `leftOuterJoin` to identify transactions with no matching product. |
| Functional error handling | `ecommerce.parsing.TransactionParser`, `ecommerce.parsing.ProductParser` | `parseLine` | Returns `Either[String, A]`: `Right(value)` for a successful parse, `Left(errorMessage)` for an expected validation failure — no exceptions are thrown for normal invalid input. |

## Required Spark Operations (preserved)

| Operation | Where |
|---|---|
| `map` | `TransactionRDDParser.parseResults`, `TransactionAnalytics.revenueByCategory`, `ProductAnalytics.revenueByProduct`, and others |
| `flatMap` | `TransactionRDDParser.validTransactions`/`errors`, `ProductAnalytics.transactionsWithMissingProducts` |
| `filter` | `Main` (`expensiveTransactions`, `expensiveElectronics`) |
| `reduceByKey` | `TransactionAnalytics.revenueByCategory`/`purchasesByCountry`, `ProductAnalytics.revenueByProduct`/`quantitySoldByProduct`/`revenueByProductCategory` |
| `join` | `ProductAnalytics.revenueByProduct`, `quantitySoldByProduct`, `revenueByProductCategory` |
| `leftOuterJoin` | `ProductAnalytics.transactionsWithMissingProducts` |

No large RDD is ever `collect()`-ed to local memory; only small, bounded
results (e.g. `takeOrdered` for the Top 5 products) leave the distributed
Spark world.
