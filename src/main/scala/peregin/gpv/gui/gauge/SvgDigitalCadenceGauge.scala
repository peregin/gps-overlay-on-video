package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

class SvgDigitalCadenceGauge extends SvgDigitalGauge {

  lazy val dummy = InputValue(Some(70), MinMax(0, 130))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = { input = sonda.cadence }

  override def imagePath = "images/cadence.svg"

  override def valueText = input.current.map(v => f"${v}%2.0f").getOrElse("")

  override def unitText = "RPM"
}
