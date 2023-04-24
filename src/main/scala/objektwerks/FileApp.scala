package objektwerks

import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.ByteString

import com.typesafe.config.ConfigFactory

import java.nio.file.Path

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.stream.scaladsl.Keep

object FileApp {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem.create("file-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher: ExecutionContext = system.dispatcher
    println("*** akka system started")

    val licenseFileSource = FileIO.fromPath(Path.of("./LICENSE"))
    val licenseFileSink = FileIO.toPath(Path.of("./target/copy.of.license.txt"))
    Await.result(
      awaitable = licenseFileSource.runWith(licenseFileSink),
      atMost = 2 seconds
    )
    println("*** see copy of license file at /target/copy.of.license.txt")

    val numbersSource = Source(1 to 10)
    val evensSquaredFlow = Flow[Int]
      .filter(i => i % 2 == 0)
      .map(i => i * 2)
      .map(i => ByteString(s"$i\n"))
    val evensSquaredFileSink = FileIO.toPath(Path.of("./target/evens.squared.txt"))
    Await.result(
      awaitable = numbersSource
                    .via(evensSquaredFlow)
                    .toMat(evensSquaredFileSink)(Keep.right)
                    .run(),
      atMost = 2 seconds
    )
    println("*** see copy of evens squared file at /target/evens.squared.txt")

    Await.result(system.terminate(), 2 seconds)
    println("*** akka system terminated")

    println("*** see log at /target/app.log")
    println("*** app shutdown")
  }
}