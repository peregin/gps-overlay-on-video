package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}
import peregin.gpv.util.UnitConverter


trait DigitalElevationGauge extends DigitalGauge {

  lazy val dummy = InputValue(689, MinMax(432, 1252))
  override def defaultInput = dummy
  override def sample(sonda: Sonda) {input = sonda.elevation}

  override def valueText() = f"${UnitConverter.elevation(input.current, units)}%2.0f"

  override def unitText() = UnitConverter.elevationUnits(units)
}
