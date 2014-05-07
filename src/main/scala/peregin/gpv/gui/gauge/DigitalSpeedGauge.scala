package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}


trait DigitalSpeedGauge extends DigitalGauge {

  lazy val dummy = InputValue(23.52, MinMax(0, 71))
  override def defaultInput = dummy
  override def sample(sonda: Sonda) {input = sonda.speed}

  override def valueText() = f"${input.current}%2.1f"

  override def unitText() = "km/h"
}
