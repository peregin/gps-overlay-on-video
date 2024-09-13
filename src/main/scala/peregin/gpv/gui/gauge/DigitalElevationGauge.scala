package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}
import peregin.gpv.util.UnitConverter


class DigitalElevationGauge extends DigitalGauge {

  lazy val dummy = InputValue(Some(689), MinMax(432, 1252))
  override def defaultInput = dummy
  override def sample(sonda: Sonda): Unit = input = sonda.elevation

  override def valueText() = input.current.map(v => f"${UnitConverter.elevation(v, units)}%2.0f").getOrElse("")

  override def unitText() = UnitConverter.elevationUnits(units)
}
