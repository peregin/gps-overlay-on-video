package peregin.gpv.manual

import info.BuildInfo
import peregin.gpv.gui.gauge._
import peregin.gpv.gui.{GaugeTestPanel, Goodies}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Logging}

import scala.swing._


object GaugeManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  lazy val sample = Telemetry.load(Io.getResource("gps/sample.gpx")) // in case if it is needed

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new GaugeTestPanel({
      val comp = new GaugeComponent with IconicHeartRateGauge //RadialSpeedGauge // ElevationChart
      comp match {
        case chart: ChartPainter => chart.telemetry = sample
        case _ => // it is not needed to update the track
      }
      comp
    })
  }
  override def top = frame

  Goodies.center(frame)
}
