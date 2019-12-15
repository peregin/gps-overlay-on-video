package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

trait SvgPowerGauge extends SvgGauge {

  lazy val dummy = InputValue(121, MinMax(0, 453))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = sonda.power.foreach(input = _)

  override def imagePath = "images/power.svg"

  override def valueText = f"${input.current}%2.0f"

  override def unitText = "W"
}
