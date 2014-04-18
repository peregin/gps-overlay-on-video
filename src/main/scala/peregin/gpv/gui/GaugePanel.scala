package peregin.gpv.gui

import scala.swing.GridPanel
import peregin.gpv.gui.gauge.{HeartRateGauge, ElevationGauge, CadenceGauge, SpeedGauge}


class GaugePanel extends GridPanel(0, 5) {

  contents += new SpeedGauge
  contents += new CadenceGauge
  contents += new ElevationGauge
  contents += new HeartRateGauge
}
