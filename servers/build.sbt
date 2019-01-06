import sbt._
import sbt.Keys._
import Dependency._

val jfrContinuous = Seq("-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder",
  "-XX:FlightRecorderOptions=defaultrecording=true,disk=true,maxage=10h,dumponexit=true,loglevel=info")

val jfrWithMemAndExceptions = Seq("-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder", "-XX:+UnlockDiagnosticVMOptions",
  "-XX:+DebugNonSafepoints", "-XX:StartFlightRecording=delay=2s,duration=60m,name=mem_ex,filename=recording.jfr,settings=profile_heap",
  "-XX:FlightRecorderOptions=disk=true,maxage=10h,dumponexit=true,loglevel=info")

// initial Java heap size: 1G, maximum Java heap size: 2G
val heapOptions = Seq("-Xms1G", "-Xmx2G")

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.8"
)

lazy val `common` =
  (project in file("common"))
    .enablePlugins(AkkaGrpcPlugin)
    .settings(
      name := "common",
      commonSettings
    )

lazy val `akka-http` =
  (project in file("akka-http"))
    .settings(
      commonSettings,
      libraryDependencies ++= Seq(AkkaHttp, circeForAkka, circeGeneric),
      fork in run := true,
      javaOptions in run ++= heapOptions ++ jfrWithMemAndExceptions
    )
    .dependsOn(`common`)

lazy val `akka-grpc` =
  (project in file("akka-grpc"))
    .enablePlugins(AkkaGrpcPlugin)
    .enablePlugins(JavaAgent)
    .settings(
      commonSettings,
      fork in run := true,
      javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test",
      javaOptions in run ++= heapOptions ++ jfrWithMemAndExceptions
    )
    .dependsOn(`common`)

lazy val root = Project("akka-http-grpc", file("."))
  .aggregate(`common`, `akka-http`, `akka-grpc`)