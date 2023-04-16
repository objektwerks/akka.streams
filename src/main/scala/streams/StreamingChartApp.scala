package streams

import akka.actor.ActorSystem
import akka.stream.scaladsl._

import com.typesafe.config.ConfigFactory

import java.awt.{BorderLayout, EventQueue}

import javax.swing.{BorderFactory, JFrame, WindowConstants}
import javax.swing.UIManager._

import org.jfree.chart.ChartPanel
import org.jfree.data.time.{TimeSeries, TimeSeriesDataItem}
import org.jfree.data.time.Millisecond

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object StreamingChartApp {
  def main(args: Array[String]): Unit = {
    val timeSeries = new TimeSeries("Time")

    EventQueue.invokeLater( () => {
        setLookAndFeel(getSystemLookAndFeelClassName)

        val chart = StreamingChart(timeSeries)
        val chartPanel = new ChartPanel( chart.jFreeChart )
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15))

        val frame = new JFrame()
        frame.setTitle("Streaming Chart App")
        frame.setSize(900, 600)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setLocationRelativeTo(null)
        frame.add(chartPanel, BorderLayout.CENTER)
        frame.setVisible(true)
      })

    implicit val system = ActorSystem.create("streaming-chart-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher = system.dispatcher

    def addOrUpdate(timeSeries: TimeSeries): Unit =
      timeSeries.addOrUpdate( new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() ) )

    def addOrUpdateAsRunnable(timeSeries: TimeSeries): Runnable =
      () => timeSeries.addOrUpdate( new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() ) )

    // 1. Update time series with akka stream.
    Source
      .tick(1 second, 1 second, ())
      .map( _ => addOrUpdate(timeSeries) )
      .runWith(Sink.ignore)

    // 2. Update time series with akka scheduler.
    system
      .scheduler
      .scheduleWithFixedDelay(2 seconds, 2 seconds)( addOrUpdateAsRunnable(timeSeries) )

    sys.addShutdownHook {
      Await.result(system.terminate(), 30 seconds)
    }
  }
}