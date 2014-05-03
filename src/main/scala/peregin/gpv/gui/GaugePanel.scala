package peregin.gpv.gui

import scala.swing.GridPanel
import peregin.gpv.gui.gauge._


class GaugePanel extends GridPanel(0, 5) {

  contents += new GaugeComponent with RadialSpeedGauge
  contents += new GaugeComponent with CadenceGauge
  contents += new GaugeComponent with IconicElevationGauge
  contents += new GaugeComponent with LinearElevationGauge
  contents += new GaugeComponent with HeartRateGauge
  contents += new GaugeComponent with DigitalSpeedGauge
}
