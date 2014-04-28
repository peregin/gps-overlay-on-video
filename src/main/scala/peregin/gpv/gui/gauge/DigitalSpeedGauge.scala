package peregin.gpv.gui.gauge

import peregin.gpv.model.{MinMax, InputValue}
import java.awt.{Font, Graphics2D}


class DigitalSpeedGauge extends GaugeComponent with DigitalFont {

  val dummy = InputValue(23.52, MinMax(0, 71))
  override def defaultInput = dummy

  override def paint(g: Graphics2D) = {
    super.paint(g)

    val w = peer.getWidth
    val h = peer.getHeight
    val cy = h / 2
    val box = math.min(w, h)
    val fs = box / 2

    // draw current speed
    g.setFont(digitalFont.deriveFont(Font.BOLD, fs.toFloat))
    val text = f"${input.current}%2.1f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, (w - tb.getWidth) / 2, cy + box / 2 - tb.getHeight * 1.2)
    // draw unit
    g.setFont(digitalFont.deriveFont(Font.BOLD, fs / 4))
    val utext = "km/h"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, (w - utb.getWidth) / 2, cy + box / 2 + utb.getHeight * 2 - tb.getHeight * 1.2)
  }
}
