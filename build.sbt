1name := "akka.streams"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.18"
libraryDependencies ++= {
  val akkaVersion = "2.6.21" // Don't upgrade due to BUSL 1.1!
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.jfree" % "jfreechart" % "1.5.6",
    "com.formdev" % "flatlaf" % "3.7",
    "ch.qos.logback" % "logback-classic" % "1.5.24",
    "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
}
