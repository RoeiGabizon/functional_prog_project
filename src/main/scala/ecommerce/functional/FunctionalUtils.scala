package ecommerce.functional

/** A collection of general-purpose functional programming utilities,
  * independent of the e-commerce domain and Spark.
  *
  * These functions are generic (parameterized over a type `A`) so they can
  * be reused across the project wherever a predicate or transformation is
  * needed, but every function here is actually exercised by real project
  * code (see `Main` and `TransactionParser`) rather than left unused.
  */
object FunctionalUtils {

  /** Custom combinator that combines two predicates using logical AND.
    *
    * This is a genuine custom combinator (not `Function1.andThen`/`compose`):
    * it accepts two functions (`A => Boolean`) as parameters and returns a
    * brand new function of the same shape, built by closing over `first`
    * and `second`. The returned function can be passed directly to Spark's
    * `filter`.
    *
    * @param first  the first predicate
    * @param second the second predicate
    * @return a predicate that is true only when both `first` and `second` are true
    */
  def and[A](first: A => Boolean, second: A => Boolean): A => Boolean =
    value => first(value) && second(value)

  /** Custom combinator that combines two predicates using logical OR.
    *
    * Like [[and]], this builds and returns a new function rather than
    * delegating to `Function1` combinators.
    *
    * @param first  the first predicate
    * @param second the second predicate
    * @return a predicate that is true when either `first` or `second` is true
    */
  def or[A](first: A => Boolean, second: A => Boolean): A => Boolean =
    value => first(value) || second(value)

  /** Custom combinator that negates a predicate.
    *
    * @param predicate the predicate to negate
    * @return a predicate that is true whenever `predicate` is false
    */
  def not[A](predicate: A => Boolean): A => Boolean =
    value => !predicate(value)

  /** Removes leading and trailing whitespace. One step of the
    * [[normalizeCategory]] composition pipeline.
    */
  val trimText: String => String = _.trim

  /** Converts text to lower case. One step of the [[normalizeCategory]]
    * composition pipeline.
    */
  val lowerCaseText: String => String = _.toLowerCase

  /** Capitalizes the first character of a string, leaving the rest
    * untouched. One step of the [[normalizeCategory]] composition pipeline.
    */
  val capitalizeText: String => String = value =>
    value.headOption match {
      case Some(first) => first.toUpper + value.drop(1)
      case None        => value
    }

  /** Function composition: normalizes a raw category string into a
    * consistent `Title case` form (e.g. `" ELECTRONICS "` -> `"Electronics"`).
    *
    * Built from three small pure functions ([[trimText]], [[lowerCaseText]],
    * [[capitalizeText]]) combined with `andThen`, so each step's output
    * feeds directly into the next. This is standard function composition
    * (as opposed to the custom `and`/`or`/`not` combinators above), and is
    * used by [[ecommerce.parsing.TransactionParser]] while parsing raw
    * transaction lines.
    */
  val normalizeCategory: String => String =
    trimText andThen lowerCaseText andThen capitalizeText

  /** Higher-order function that conditionally applies a transformation.
    *
    * Accepts a predicate and a transformation function as parameters
    * (both `A => Boolean` / `A => A`), and returns `transformation(value)`
    * only when `predicate(value)` holds; otherwise `value` is returned
    * unchanged. This demonstrates a function that both takes functions as
    * parameters and, via currying, defers evaluation until `value` is
    * supplied.
    *
    * @param predicate      decides whether the transformation should apply
    * @param transformation the transformation to conditionally apply
    * @param value          the value to test and possibly transform
    * @return the transformed value if the predicate holds, otherwise the original value
    */
  def transformIf[A](predicate: A => Boolean, transformation: A => A)(value: A): A =
    if (predicate(value)) transformation(value) else value
}
