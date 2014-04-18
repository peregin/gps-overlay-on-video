package peregin.gpv.gui.gauge

import peregin.gpv.model.{MinMax, InputValue}
import java.awt.{Font, Color, Graphics2D}


class ElevationGauge extends GaugeComponent {

  val dummy = InputValue(689, MinMax(432, 1252))
  override def defaultInput = dummy

  override def paint(g: Graphics2D) = {
    val w = peer.getWidth
    val h = peer.getHeight
    val box = math.min(w, h)
    val cx = w / 2
    val cy = h / 2

    g.setColor(Color.yellow)
    g.drawRoundRect(1, 1, w - 2, h - 2, 5, 5)

    // draw current speed
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 4).toFloat))
    val text = f"${input.current}%2.0f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, (w - tb.getWidth) / 2, (h + tb.getHeight) / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 10).toFloat))
    val utext = "m"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, (w - utb.getWidth) / 2, cy + utb.getHeight * 1.8)
  }
}
