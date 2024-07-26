package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.UnitConverter

class SvgElevationGauge extends SvgGauge {

  lazy val dummy = InputValue(689, MinMax(432, 1252))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = input = sonda.elevation

  override def imagePath = "images/mountain.svg"

  override def valueText = f"${input.current}%2.0f"

  override def unitText = UnitConverter.elevationUnits(units)
}
