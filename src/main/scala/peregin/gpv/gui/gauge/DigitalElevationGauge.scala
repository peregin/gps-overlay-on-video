package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}


trait DigitalElevationGauge extends DigitalGauge {

  lazy val dummy = InputValue(689, MinMax(432, 1252))
  override def defaultInput = dummy
  override def sample(sonda: Sonda) {input = sonda.elevation}

  override def valueText() = f"${input.current}%2.0f"

  override def unitText() = "m"
}
