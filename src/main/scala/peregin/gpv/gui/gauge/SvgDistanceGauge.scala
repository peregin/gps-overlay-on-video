package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.UnitConverter

class SvgDistanceGauge extends SvgGauge {

  lazy val dummy = InputValue(Some(80.21), MinMax(0, 123.4))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = input = sonda.distance

  override def imagePath = "images/road.svg"

  override def valueText = input.current.map(v => f"${v}%2.1f").getOrElse("")

  override def unitText = UnitConverter.distanceUnits(units)
}
