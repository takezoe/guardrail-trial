name := "petstore-server"
version := "1.0"
scalaVersion := "2.12.5"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
libraryDependencies ++= {
  val akkaHttpV   = "10.0.10"
  val catsVersion = "0.9.0"
  val circeVersion = "0.8.0"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-java8" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "org.typelevel" %% "cats" % catsVersion
  )
}
