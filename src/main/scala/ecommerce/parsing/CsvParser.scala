package ecommerce.parsing

/** Common contract for pure CSV parsers.
  *
  * Defines a single, generic operation for turning a raw CSV line into a
  * domain value `A`, using `Either` for functional error handling: `Right(A)`
  * for a successful parse, `Left(String)` for an expected validation failure.
  * Implementations (e.g. [[TransactionParser]], [[ProductParser]]) must
  * remain pure and Spark-free; this trait itself holds no state and no
  * Spark dependency, only the shared abstraction.
  *
  * @tparam A the domain type produced by a successful parse
  */
trait CsvParser[A] {

  /** Parses a single raw CSV line into a value of type `A`.
    *
    * @param line a raw, comma-separated CSV line (without header)
    * @return `Right(A)` on success, or `Left(errorMessage)` on failure
    */
  def parseLine(line: String): Either[String, A]
}
