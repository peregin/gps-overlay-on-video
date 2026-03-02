package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

class SvgDigitalTemperatureGauge extends SvgDigitalGauge {

  lazy val dummy = InputValue(Some(25), MinMax(0, 43))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = { input = sonda.temperature }

  override def imagePath = "images/temperature.svg"

  override def valueText = input.current.map(v => f"${v}%2.0f").getOrElse("")

  override def unitText = "C"
}
