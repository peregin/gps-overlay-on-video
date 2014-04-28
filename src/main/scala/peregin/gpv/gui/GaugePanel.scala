package peregin.gpv.gui

import scala.swing.GridPanel
import peregin.gpv.gui.gauge._


class GaugePanel extends GridPanel(0, 5) {

  contents += new RadialSpeedGauge
  contents += new CadenceGauge
  contents += new IconicElevationGauge
  contents += new LinearElevationGauge
  contents += new HeartRateGauge
  contents += new DigitalSpeedGauge
}
