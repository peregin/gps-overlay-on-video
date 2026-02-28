package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.UnitConverter

class DigitalCadenceGauge extends DigitalGauge {

  lazy val dummy = InputValue(Some(75), MinMax(0, 130))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = input = sonda.cadence

  override def valueText() = input.current.map(v => f"${v}%2.0f").getOrElse("")

  override def unitText() = UnitConverter.cadenceUnits()
}
