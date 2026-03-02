package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.UnitConverter

class SvgDigitalElevationGauge extends SvgDigitalGauge {

  lazy val dummy = InputValue(Some(689), MinMax(0, 1300))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = input = sonda.elevation

  override def imagePath = "images/mountain.svg"

  override def valueText = input.current.map(v => f"${v}%2.0f").getOrElse("")

  override def unitText = UnitConverter.elevationUnits(units)
}
