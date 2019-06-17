package stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, SourceShape, Supervision}
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncFunSuite, BeforeAndAfterAll, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class StreamTest extends AsyncFunSuite with BeforeAndAfterAll with Matchers {
  implicit val system = ActorSystem.create("stream", ConfigFactory.load("test.conf"))
  implicit val dispatcher = system.dispatcher
  val decider: Decider = Supervision.restartingDecider
  val settings = ActorMaterializerSettings(system).withSupervisionStrategy(decider)
  implicit val materializer = ActorMaterializer(settings)

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].filter(_ % 2 == 0).map(_ * 2)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  override protected def afterAll(): Unit = {
    import scala.language.postfixOps
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
  }

  test("graph dsl") {
    val source = Source.fromGraph( GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val source1 = Source(1 to 10)
      val source2 = Source(1 to 10)

      val merge = builder.add( ZipWith( (a:Int, b:Int) => { a + b } ) )

      source1 ~> merge.in1
      source2 ~> merge.in0

      SourceShape(merge.out)
    } )
    source.runWith(sink) map { _ shouldBe 110 }
  }
}