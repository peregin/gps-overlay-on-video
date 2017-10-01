package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.UnitConverter

trait SvgDistanceGauge extends SvgGauge {

  lazy val dummy = InputValue(80.21, MinMax(0, 123.4))
  override def defaultInput = dummy

  override def sample(sonda: Sonda) {input = sonda.distance}

  override def imagePath = "images/road.svg"

  override def valueText = f"${input.current}%2.1f"

  override def unitText = UnitConverter.distanceUnits(units)
}
