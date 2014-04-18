package peregin.gpv.gui.gauge

import java.awt.Color
import scala.swing.Graphics2D
import peregin.gpv.model.{MinMax, InputValue}


class DummyGauge extends GaugeComponent {

  val dummy = InputValue(18, MinMax(0, 27))
  override def defaultInput = dummy

  override def paint(g: Graphics2D) = {
    val w = peer.getWidth
    val h = peer.getHeight

    g.setColor(Color.yellow)
    g.drawRoundRect(1, 1, w - 2, h - 2, 5, 5)

    g.setFont(gaugeFont)
    val text = "Speed Gauge - 18 km/h"
    val fm = g.getFontMetrics(gaugeFont)
    val sw = fm.stringWidth(text)
    textWidthShadow(g, text, (w - sw) / 2, (h + fm.getHeight) / 2)
  }
}
