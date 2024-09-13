package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

class SvgHeartRateGauge extends SvgGauge {

  lazy val dummy = InputValue(Some(89), MinMax(30, 230))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = { input = sonda.heartRate }

  override def imagePath = "images/heart.svg"

  override def valueText = input.current.map(v => f"${v}%2.0f").getOrElse("")

  override def unitText = "bpm"
}
