package objektwerks

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Sink, Source}

import com.typesafe.config.ConfigFactory

import java.nio.file.Path

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.Codec
import scala.language.postfixOps
import scala.util.{Try, Using}

object FileApp {
  def fileToLines(file: String): Try[Seq[String]] = {
    Using( scala.io.Source.fromFile(file, Codec.UTF8.name) ) {
      source => source.getLines().toSeq 
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem.create("file-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher: ExecutionContext = system.dispatcher
    println("*** akka system started")

    val source = FileIO.fromPath(Path.of("./LICENSE"))
    val sink = FileIO.toPath(Path.of("./target/license.txt"))
    Await.result( source.runWith(sink), 10 seconds )
    println("*** see new license file at /target/license.txt")

    Await.result(system.terminate(), 10 seconds)
    println("*** akka system terminated")

    println("*** see log at /target/app.log")
    println("*** app shutdown")
  }
}