name := "FunctionalECommercePipeline"

version := "0.1.0"

scalaVersion := "2.12.19"

// Spark 3.3.x is compatible with Scala 2.12 and JDK 11.
val sparkVersion = "3.3.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql"  % sparkVersion,
  "org.scalatest"    %% "scalatest"  % "3.2.18" % Test
)

// Avoid duplicate resource conflicts when assembling/running Spark locally.
Compile / run / fork := true
Test / fork := true

javacOptions ++= Seq("-source", "11", "-target", "11")
