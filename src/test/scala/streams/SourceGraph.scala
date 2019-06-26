package streams

import akka.stream.SourceShape
import akka.stream.scaladsl.{GraphDSL, Source, ZipWith}

object SourceGraph {
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
}