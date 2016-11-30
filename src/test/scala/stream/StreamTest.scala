package stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class StreamTest extends FunSuite with BeforeAndAfterAll {
  implicit val system: ActorSystem = ActorSystem.create("stream", ConfigFactory.load("test.conf"))
  implicit val ec = system.dispatcher
  val decider: Decider = Supervision.resumingDecider
  val settings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(settings)

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
  val flow: Flow[Int, Int, NotUsed] = Flow.fromSinkAndSource(sink, source)
  val graph: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
  }

  test("run foreach") {
    source.runForeach { value => assert(value >= 1 && value <= 10) }
  }

  test("run fold") {
    source.runFold(0)(_ + _) map { sum => assert(sum == 10) }
  }

  test("run reduce") {
    source.runReduce(_ + _) map { sum => assert(sum == 10) }
  }

  test("run with") {
    source.runWith(sink)
    source.runWith(Sink.fold(0)(_ + _)) map { sum => assert(sum == 10) }
    source.runWith(Sink.foreach { value => assert(value >= 1 && value <= 10) })
    flow.runWith(source, sink)._2 map { sum => assert(sum == 10) }
  }

  test("run") {
    graph.run map { sum => assert(sum == 10) }
  }
}