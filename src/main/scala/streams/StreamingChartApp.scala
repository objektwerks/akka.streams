package streams

import java.awt.{BorderLayout, EventQueue}

import javax.swing.{JFrame, WindowConstants}
import javax.swing.UIManager._

import org.jfree.chart.ChartPanel

object StreamingChartApp {
  def main(args: Array[String]): Unit = {
    EventQueue.invokeLater( new Runnable() {
      override def run(): Unit = {
        setLookAndFeel(getSystemLookAndFeelClassName)
        val frame = new JFrame()
        frame.setTitle("Streaming Chart App")
        frame.setSize(600, 600)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setLocationRelativeTo(null)
        frame.add(new ChartPanel(StreamingChart.build), BorderLayout.CENTER)
        frame.setVisible(true)
      }
    })
  }
}