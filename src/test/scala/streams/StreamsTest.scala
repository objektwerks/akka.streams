package streams

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, SinkShape, SourceShape}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class StreamsTest extends FunSuite with BeforeAndAfterAll with Matchers {
  implicit val system = ActorSystem.create("streams", ConfigFactory.load("test.conf"))
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 3 seconds)
    ()
  }

  test("source") {
    val source = Source(1 to 10)
    source.runFold(0)(_ + _) map { _ shouldBe 55 }
    source.runReduce(_ + _) map { _ shouldBe 55 }
  }

  test("source ~ sink") {
    val source = Source(1 to 10)
    val sink = Sink.fold[Int, Int](0)(_ + _)
    source.toMat(sink)(Keep.right).run map { _ shouldBe 55 }
    source.runWith(sink) map { _ shouldBe 55 }
  }

  test("source ~ flow ~ sink") {
    val source = Source(1 to 10)
    val flow = Flow[Int].filter(_ % 2 == 0).map(_ * 2)
    val sink = Sink.fold[Int, Int](0)(_ + _)
    source.via(flow).toMat(sink)(Keep.right).run map { _ shouldBe 60 }
    source.via(flow).runWith(sink) map { _ shouldBe 60 }
    flow.runWith(source, sink)._2 map { _ shouldBe 60 }
  }

  test("graph") {
    val source = Source(1 to 10)
    val incrementer = Flow[Int].map(_ + 1)
    val multiplier = Flow[Int].map(_ * 2)
    val sink = Sink.reduce[(Int, Int)]( (a, b) => (a._1 + a._2, b._1 + b._2) )

    val graph = RunnableGraph.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Int](2))
        val zip = builder.add(Zip[Int, Int])

        source ~> broadcast
        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1
        zip.out ~> sink

        ClosedShape
      }
    )
    graph.run
  }

  test("source graph") {
    val sourceGraph = Source.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val source1 = Source(1 to 10)
        val source2 = Source(1 to 10)

        val merge = builder.add( ZipWith( (a: Int, b: Int) => { a + b } ) )

        source1 ~> merge.in0
        source2 ~> merge.in1

        SourceShape(merge.out)
      }
    )
    val sink = Sink.reduce[Int](_ + _)
    sourceGraph.runWith(sink) map { _ shouldBe 110 }
  }

  test("sink graph") {
    val sinkGraph = Sink.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val sink1 = Sink.reduce[Int](_ + _)
        val sink2 = Sink.reduce[Int](_ + _)

        val broadcast = builder.add(Broadcast[Int](2))
        broadcast ~> sink1
        broadcast ~> sink2

        SinkShape(broadcast.in)
      }
    )
    val source = Source(1 to 10)
    sinkGraph.runWith(source)
  }
}