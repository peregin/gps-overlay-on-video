package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}
import peregin.gpv.util.UnitConverter


class DigitalSpeedGauge extends DigitalGauge {

  lazy val dummy = InputValue(Some(23.52), MinMax(0, 71))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = input = sonda.speed

  override def valueText() = input.current.map(v => f"${UnitConverter.distance(v, units)}%2.1f").getOrElse("")

  override def unitText() = UnitConverter.distanceUnits(units)
}
