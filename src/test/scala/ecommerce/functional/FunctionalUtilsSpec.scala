package ecommerce.functional

import org.scalatest.funsuite.AnyFunSuite

class FunctionalUtilsSpec extends AnyFunSuite {

  // --- Custom combinator: and/or/not ---

  test("and returns true only when both predicates are true") {
    val isPositive: Int => Boolean = _ > 0
    val isEven: Int => Boolean = _ % 2 == 0

    val positiveAndEven = FunctionalUtils.and(isPositive, isEven)

    assert(positiveAndEven(4))
    assert(!positiveAndEven(-4))
    assert(!positiveAndEven(3))
    assert(!positiveAndEven(-3))
  }

  test("or returns true when at least one predicate is true") {
    val isPositive: Int => Boolean = _ > 0
    val isEven: Int => Boolean = _ % 2 == 0

    val positiveOrEven = FunctionalUtils.or(isPositive, isEven)

    assert(positiveOrEven(3))
    assert(positiveOrEven(-4))
    assert(!positiveOrEven(-3))
  }

  test("not negates a predicate") {
    val isPositive: Int => Boolean = _ > 0
    val isNotPositive = FunctionalUtils.not(isPositive)

    assert(isNotPositive(-1))
    assert(!isNotPositive(1))
  }

  // --- Function composition: normalizeCategory ---

  test("normalizeCategory trims, lower-cases, and capitalizes the input") {
    assert(FunctionalUtils.normalizeCategory(" ELECTRONICS ") == "Electronics")
    assert(FunctionalUtils.normalizeCategory("books") == "Books")
    assert(FunctionalUtils.normalizeCategory(" gaming") == "Gaming")
  }

  test("normalizeCategory returns an empty string for empty input") {
    assert(FunctionalUtils.normalizeCategory("") == "")
  }

  // --- Higher-order function: transformIf ---

  test("transformIf applies the transformation when the predicate is true") {
    val isNegative: Int => Boolean = _ < 0
    val negate: Int => Int = -_

    assert(FunctionalUtils.transformIf(isNegative, negate)(-5) == 5)
  }

  test("transformIf leaves the value unchanged when the predicate is false") {
    val isNegative: Int => Boolean = _ < 0
    val negate: Int => Int = -_

    assert(FunctionalUtils.transformIf(isNegative, negate)(5) == 5)
  }
}
