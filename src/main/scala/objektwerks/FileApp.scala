package objektwerks

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Sink, Source}

import com.typesafe.config.ConfigFactory

import java.nio.file.Path

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

object FileApp {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem.create("file-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher: ExecutionContext = system.dispatcher
    println("*** akka system started")

    val licenseFileSource = FileIO.fromPath(Path.of("./LICENSE"))
    val licenseFileSink = FileIO.toPath(Path.of("./target/copy.of.license.txt"))
    Await.result( licenseFileSource.runWith(licenseFileSink), 2 seconds )
    println("*** see copy of license file at /target/copy.of.license.txt")

    Await.result(system.terminate(), 2 seconds)
    println("*** akka system terminated")

    println("*** see log at /target/app.log")
    println("*** app shutdown")
  }
}