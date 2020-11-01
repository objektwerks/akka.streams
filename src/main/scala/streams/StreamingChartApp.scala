package streams

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source

import com.typesafe.config.ConfigFactory

import java.awt.{BorderLayout, EventQueue}

import javax.swing.{JFrame, WindowConstants}
import javax.swing.UIManager._

import org.jfree.chart.ChartPanel
import org.jfree.data.time.{TimeSeries, TimeSeriesDataItem}
import org.jfree.data.time.Millisecond

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object StreamingChartApp {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem.create("streaming-chart-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher = system.dispatcher

    val chart = StreamingChart()
    val timeSeries = chart.timeSeries
    val chartPanel = new ChartPanel( chart.jFreeChart )

    EventQueue.invokeLater( new Runnable() {
      override def run(): Unit = {
        setLookAndFeel(getSystemLookAndFeelClassName)
        val frame = new JFrame()
        frame.setTitle("Streaming Chart App")
        frame.setSize(600, 600)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setLocationRelativeTo(null)
        frame.add(chartPanel, BorderLayout.CENTER)
        frame.setVisible(true)
      }
    })

    Source.tick(1 second, 300 milli, addOrUpdate(timeSeries)).run()

    val cancellable = system.scheduler.scheduleAtFixedRate(1 second, 600 milli)( addOrUpdate(timeSeries) )
    println(cancellable.toString)
  }

  def addOrUpdate(timeSeries: TimeSeries): Runnable = new Runnable() {
    override def run(): Unit = {
      val item = new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() )
      timeSeries.addOrUpdate(item)
      ()
    }
  }
}