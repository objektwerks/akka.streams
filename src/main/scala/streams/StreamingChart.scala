package streams

import java.text.SimpleDateFormat

import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.{DateAxis, NumberAxis}
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.time.{TimeSeries, TimeSeriesCollection}

object StreamingChart {
  def apply(): StreamingChart = new StreamingChart()
}

class StreamingChart {
  val xyPlot = new XYPlot()

  val timeSeries = new TimeSeries("Streaming Chart")
  val timeSeriesCollection = new TimeSeriesCollection( timeSeries )
  xyPlot.setDataset(0, timeSeriesCollection)

  val renderer = new XYLineAndShapeRenderer()
  renderer.setDefaultShapesVisible(true)
  renderer.setDefaultItemLabelsVisible(true)
  xyPlot.setRenderer(0, renderer)

  val xAxis = new DateAxis("Time")
  xAxis.setDateFormatOverride( new SimpleDateFormat("H:mm:ss") )
  xyPlot.setDomainAxis(0, xAxis)

  val yAxis = new NumberAxis("Values")
  xyPlot.setRangeAxis(yAxis)

  val jFreeChart = new JFreeChart("Streaming Chart", JFreeChart.DEFAULT_TITLE_FONT, xyPlot, true)
}