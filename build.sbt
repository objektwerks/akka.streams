name := "akka.streams"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.2"
libraryDependencies ++= {
  val akkaVersion = "2.6.6"
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.scalatest" %% "scalatest" % "3.1.2" % Test
  )
}