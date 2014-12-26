package peregin.gpv.manual

import info.BuildInfo
import peregin.gpv.gui.gauge.{GaugeComponent, RadialSpeedGauge}
import peregin.gpv.gui.{GaugeTestPanel, Goodies}
import peregin.gpv.util.Logging

import scala.swing._


object GaugeManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new GaugeTestPanel(new GaugeComponent with RadialSpeedGauge)
  }
  override def top = frame

  Goodies.center(frame)
}
