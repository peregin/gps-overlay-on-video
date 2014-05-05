package peregin.gpv.gui.gauge

import scala.swing.Graphics2D
import peregin.gpv.model.{Sonda, MinMax, InputValue}


trait DummyGauge extends GaugePainter {

  lazy val dummy = InputValue(18, MinMax(0, 27))
  override def defaultInput = dummy
  override def sample(sonda: Sonda) {input = sonda.speed}

  override def paint(g: Graphics2D, w: Int, h: Int) {
    super.paint(g, w, h)

    g.setFont(gaugeFont)
    val text = "Speed Gauge - 18 km/h"
    val fm = g.getFontMetrics(gaugeFont)
    val sw = fm.stringWidth(text)
    textWidthShadow(g, text, (w - sw) / 2, (h + fm.getHeight) / 2)
  }
}
