package peregin.gpv.manual

import info.BuildInfo
import peregin.gpv.gui.gauge._
import peregin.gpv.gui.{GaugeTestPanel, Goodies}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.Logging

import scala.swing._


object GaugeManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  private lazy val sample = Telemetry.sample()

  private val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new GaugeTestPanel({
      val comp = new GaugeComponent with ElevationChart // SvgElevationGauge //SvgDistanceGauge //SvgPowerGauge //SvgHeartRateGauge // IconicHeartRateGauge //RadialSpeedGauge
      comp match {
        case chart: ChartPainter => chart.telemetry = sample
        case _ => // it is not needed to update the track
      }
      comp
    })
  }
  override def top: Frame = frame

  Goodies.center(frame)
}
