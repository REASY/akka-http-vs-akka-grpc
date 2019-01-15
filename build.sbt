import sbt._
import sbt.Keys._
import Dependency._

val jfrContinuous = Seq("-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder",
  "-XX:FlightRecorderOptions=defaultrecording=true,disk=true,maxage=10h,dumponexit=true,loglevel=info")

// On the running machine there should be file /usr/lib/jvm/java-8-oracle/jre/lib/jfr/profile_heap_exception.jfc  with content from
// https://pastebin.com/N3uuUfPz - it's Java Mission Control with metrics about heap allocation and details about exceptions
val jfrWithMemAndExceptions = Seq("-XX:+UnlockCommercialFeatures", "-XX:+FlightRecorder", "-XX:+UnlockDiagnosticVMOptions",
  "-XX:+DebugNonSafepoints", "-XX:StartFlightRecording=delay=2s,duration=60m,name=mem_ex,filename=recording.jfr,settings=profile_heap_exception",
  "-XX:FlightRecorderOptions=disk=true,maxage=10h,dumponexit=true,loglevel=info")

// initial Java heap size: 1G, maximum Java heap size: 4G
val heapOptions = Seq("-Xms1G", "-Xmx4G")

def dateFormat: String = new java.text.SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new java.util.Date())

def logGC(project: String) = Seq("-XX:+PrintGCDetails", "-XX:+PrintGCDateStamps", s"-Xloggc:gc_${project}_$dateFormat.log")

lazy val commonSettings = Seq(
  version := "0.0.1",
  scalaVersion := "2.12.8"
)

lazy val `common` =
  (project in file("common"))
    .enablePlugins(AkkaGrpcPlugin)
    .enablePlugins(JmhPlugin)
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
      javaOptions in run ++= heapOptions ++ logGC("akka-http") ++ jfrWithMemAndExceptions
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
      javaOptions in run ++= heapOptions ++ logGC("akka-grpc") ++ jfrWithMemAndExceptions
    )
    .dependsOn(`common`)

lazy val root = Project("akka-http-grpc", file("."))
  .aggregate(`common`, `akka-http`, `akka-grpc`)