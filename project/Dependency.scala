import sbt._

object Dependency {
  val AkkaHttp: ModuleID = "com.typesafe.akka" %% "akka-http" % "10.2.9"
  val circeForAkka: ModuleID = "de.heikoseeberger" %% "akka-http-circe" % "1.39.2"
  val circeGeneric: ModuleID = "io.circe" %% "circe-generic" % "0.14.1"
  val circeParser: ModuleID = "io.circe" %% "circe-parser" % "0.14.1"
}