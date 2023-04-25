name := "akka.streams"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.10"
libraryDependencies ++= {
  val akkaVersion = "2.6.20" // Don't upgrade due to BUSL 1.1!
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.jfree" % "jfreechart" % "1.5.3",
    "com.formdev" % "flatlaf" % "3.1.1",
    "ch.qos.logback" % "logback-classic" % "1.4.6",
    "org.scalatest" %% "scalatest" % "3.2.15" % Test
  )
}
