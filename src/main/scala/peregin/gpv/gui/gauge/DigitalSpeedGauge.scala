package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}
import peregin.gpv.util.UnitConverter


class DigitalSpeedGauge extends DigitalGauge {

  lazy val dummy = InputValue(23.52, MinMax(0, 71))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = input = sonda.speed

  override def valueText() = f"${UnitConverter.distance(input.current, units)}%2.1f"

  override def unitText() = UnitConverter.distanceUnits(units)
}
