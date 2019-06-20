package streams

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, SourceShape}
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncFunSuite, BeforeAndAfterAll, Matchers}

class GraphsTest extends AsyncFunSuite with BeforeAndAfterAll with Matchers {
  implicit val system = ActorSystem.create("streams", ConfigFactory.load("test.conf"))
  implicit val materializer = ActorMaterializer()

  test("source graph") {
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