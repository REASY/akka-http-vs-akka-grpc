import sbt._

object Dependency {
  val AkkaHttp: ModuleID = "com.typesafe.akka" %% "akka-http" % "10.1.5"
  val circeForAkka: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.23.0"
  val circeGeneric: ModuleID = "io.circe" %% "circe-generic" % "0.10.0"
  val circeParser: ModuleID = "io.circe" %% "circe-parser" % "0.10.0"
}