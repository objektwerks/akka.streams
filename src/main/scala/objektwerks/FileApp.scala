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

    val source = FileIO.fromPath(Path.of("./LICENSE"))
    val sink = FileIO.toPath(Path.of("./target/license.txt"))
    Await.result( source.runWith(sink), 2 seconds )
    println("*** see license file at /target/license.txt")

    Await.result(system.terminate(), 2 seconds)
    println("*** akka system terminated")

    println("*** see log at /target/app.log")
    println("*** app shutdown")
  }
}