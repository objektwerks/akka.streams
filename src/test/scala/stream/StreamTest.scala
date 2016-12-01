package stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncFunSuite, BeforeAndAfterAll, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class StreamTest extends AsyncFunSuite with BeforeAndAfterAll with Matchers {
  implicit val system: ActorSystem = ActorSystem.create("stream", ConfigFactory.load("test.conf"))
  implicit val ec = system.dispatcher
  val decider: Decider = Supervision.resumingDecider
  val settings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(settings)

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 2)
  val graph: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
  }

  test("run fold") {
    source.runFold(0)(_ + _) map { _ shouldBe 55 }
  }

  test("run reduce") {
    source.runReduce(_ + _) map { _ shouldBe 55 }
  }

  test("run with") {
    source.runWith(sink) map { _ shouldBe 55 }
    source.runWith(Sink.fold(0)(_ + _)) map { _ shouldBe 55 }
    source.runWith(Sink.reduce[Int](_ + _)) map { _ shouldBe 55 }
    flow.runWith(source, sink)._2 map { _ shouldBe 110 }
  }

  test("run") {
    graph.run map { _ shouldBe 110 }
  }
}