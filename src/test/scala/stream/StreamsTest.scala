package stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, SourceShape}
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncFunSuite, BeforeAndAfterAll, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class StreamsTest extends AsyncFunSuite with BeforeAndAfterAll with Matchers {
  implicit val system = ActorSystem.create("streams", ConfigFactory.load("test.conf"))
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].filter(_ % 2 == 0).map(_ * 2)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  override protected def afterAll(): Unit = {
    Await.result(system.terminate(), 1 second)
    ()
  }

  test("source") {
    source.runFold(0)(_ + _) map { _ shouldBe 55 }
    source.runReduce(_ + _) map { _ shouldBe 55 }
  }

  test("source ~ sink") {
    source.toMat(sink)(Keep.right).run map { _ shouldBe 55 }
    source.runWith(sink) map { _ shouldBe 55 }
  }

  test("source ~ flow ~ sink") {
    source.via(flow).toMat(sink)(Keep.right).run map { _ shouldBe 60 }
    source.via(flow).runWith(sink) map { _ shouldBe 60 }
    flow.runWith(source, sink)._2 map { _ shouldBe 60 }
  }

  test("graph dsl") {
    val source = Source.fromGraph(
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
    source.runWith(sink) map { _ shouldBe 110 }
  }
}