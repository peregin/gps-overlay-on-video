package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

class SvgHeartRateGauge extends SvgGauge {

  lazy val dummy = InputValue(89, MinMax(30, 230))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = sonda.heartRate.foreach(input = _)

  override def imagePath = "images/heart.svg"

  override def valueText = f"${input.current}%2.0f"

  override def unitText = "bpm"
}
