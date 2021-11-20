name := "akka.streams"
organization := "objektwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.7"
libraryDependencies ++= {
  val akkaVersion = "2.6.17"
  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "org.jfree" % "jfreechart" % "1.5.3",
    "ch.qos.logback" % "logback-classic" % "1.2.7",
    "org.scalatest" %% "scalatest" % "3.2.10" % Test
  )
}
