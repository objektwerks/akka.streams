package streams

import akka.stream.SinkShape
import akka.stream.scaladsl.{Broadcast, GraphDSL, Sink}

object SinkGraph {
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
}