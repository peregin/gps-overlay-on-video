package peregin.gpv.gui.gauge

import scala.swing.Graphics2D
import peregin.gpv.model.{MinMax, InputValue}


class DummyGauge extends GaugeComponent {

  val dummy = InputValue(18, MinMax(0, 27))
  override def defaultInput = dummy

  override def paint(g: Graphics2D) = {
    super.paint(g)

    val w = peer.getWidth
    val h = peer.getHeight

    g.setFont(gaugeFont)
    val text = "Speed Gauge - 18 km/h"
    val fm = g.getFontMetrics(gaugeFont)
    val sw = fm.stringWidth(text)
    textWidthShadow(g, text, (w - sw) / 2, (h + fm.getHeight) / 2)
  }
}
