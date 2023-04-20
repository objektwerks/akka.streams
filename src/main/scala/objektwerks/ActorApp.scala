package objektwerks

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import com.typesafe.config.ConfigFactory

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.language.postfixOps

final case class Work(id: Int)
final case class Processed(worker: Int)

final class Worker(id: Int) extends Actor with ActorLogging {
  log.info(s"*** worker actor $id intialized")

  def receive: Receive = {
    case work @ Work(id) => log.info(s"*** name: ${context.self.path.name} id: $id working ...")
  }
}

final class Manager(workers: Int) extends Actor with ActorLogging {
  val router = {
    val routees = (1 to workers).map { worker =>
      ActorRefRoutee( context.actorOf(Props(classOf[Worker], worker), name = s"worker-$worker") )
    }
    Router(RoundRobinRoutingLogic(), routees)
  }
  log.info("*** manager actor intialized")

  def receive: Receive = {
    case work @ Work(worker) =>
      log.info(s"*** manager actor received work: $work")
      router.route(work, sender)
      sender ! Processed(worker)
  }
}

object ActorApp {
  def main(args: Array[String]): Unit = {
    val workers = 10
    val parallelism = Runtime.getRuntime.availableProcessors

    implicit val system: ActorSystem = ActorSystem.create("akka-streams-kafka", ConfigFactory.load("app.conf"))
    implicit val dispatcher: ExecutionContext = system.dispatcher
    implicit val timeout = Timeout(10 seconds)
    val manager = system.actorOf(Props(classOf[Manager], workers), name = "manager")
    println("*** akka system started")

    println(s"*** sourcing work for $workers actor [worker] routees, with parallelism set to: $parallelism ...")
    Source(1 to workers)
      .mapAsync(parallelism) { worker =>
        (manager ? Work(worker) ).mapTo[Processed]
      }
      .map { processed =>
        println(s"*** processed work from worker: ${processed.worker}")
      }
      .runWith(Sink.ignore)
    println(s"*** once all work results have been printed, depress RETURN key to shutdown app")

    StdIn.readLine()

    Await.result(system.terminate(), 10 seconds)
    println("*** akka system terminated")
    println("*** see log at /target/app.log")
    println("*** app shutdown")
  }
}