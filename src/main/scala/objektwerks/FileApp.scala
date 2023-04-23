package objektwerks

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}

import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

object FileApp {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem.create("file-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher: ExecutionContext = system.dispatcher
    println("*** akka system started")


    println(s"*** once all words have been printed, depress RETURN key to shutdown app")

    StdIn.readLine()

    Await.result(system.terminate(), 10 seconds)
    println("*** akka system terminated")
    println("*** see log at /target/app.log")
    println("*** app shutdown")
  }
}