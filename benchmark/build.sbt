import sbt.Keys._

version := "0.0.1"
scalaVersion := "2.12.8"
name := "benchmark"

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-test-framework" % "3.0.2" % Test,
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.2" % Test,
  "com.github.phisgr" %% "gatling-grpc" % "0.2.0" % Test,
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "io.circe" %% "circe-generic" % "0.10.0",
  "io.circe" %% "circe-parser" % "0.10.0"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

enablePlugins(GatlingPlugin)