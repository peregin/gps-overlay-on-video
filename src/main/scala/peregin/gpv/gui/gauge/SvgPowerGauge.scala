package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

class SvgPowerGauge extends SvgGauge {

  lazy val dummy = InputValue(Some(121), MinMax(0, 2000))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = { input = sonda.power }

  override def imagePath = "images/power.svg"

  override def valueText = input.current.map(v => f"${v}%2.0f").getOrElse("")

  override def unitText = "W"
}
