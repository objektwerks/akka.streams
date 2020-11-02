package streams

import akka.actor.ActorSystem

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

    val timeSeries = new TimeSeries("Time")

    EventQueue.invokeLater( new Runnable() {
      override def run(): Unit = {
        setLookAndFeel(getSystemLookAndFeelClassName)

        val chart = StreamingChart(timeSeries)
        val chartPanel = new ChartPanel( chart.jFreeChart )

        val frame = new JFrame()
        frame.setTitle("Streaming Chart App")
        frame.setSize(900, 600)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setLocationRelativeTo(null)
        frame.add(chartPanel, BorderLayout.CENTER)
        frame.setVisible(true)
      }
    })

    // Does not work!
    import akka.stream.scaladsl.Source
    Source.tick(500 milli, 500 milli, addOrUpdate(timeSeries)).run()

    val cancellable = system.scheduler.scheduleWithFixedDelay(1 second, 1 second)( addOrUpdateAsRunnable(timeSeries) )

    sys.addShutdownHook {
      cancellable.cancel()
      system.terminate()
      ()
    }
    ()
  }

  def addOrUpdate(timeSeries: TimeSeries): Unit = {
    timeSeries.addOrUpdate( new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() ) )
    ()
  }

  def addOrUpdateAsRunnable(timeSeries: TimeSeries): Runnable = new Runnable() {
    override def run(): Unit = {
      timeSeries.addOrUpdate( new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() ) )
      ()
    }
  }
}