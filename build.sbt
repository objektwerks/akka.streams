name := "akka.streams"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.3"
libraryDependencies ++= {
  val akkaVersion = "2.6.10"
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.jfree" % "jfreechart" % "1.5.1",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.scalatest" %% "scalatest" % "3.2.2" % Test
  )
}