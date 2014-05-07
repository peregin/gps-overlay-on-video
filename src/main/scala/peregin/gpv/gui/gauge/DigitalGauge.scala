package peregin.gpv.gui.gauge

import java.awt.{Font, Graphics2D}


trait DigitalGauge extends GaugePainter with DigitalFont {

  def valueText(): String
  def unitText(): String

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

    val cy = h / 2
    val box = math.min(w, h)
    val fs = box / 2

    // draw current speed
    g.setFont(digitalFont.deriveFont(Font.BOLD, fs.toFloat))
    val text = valueText()
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, (w - tb.getWidth) / 2, cy + box / 2 - tb.getHeight * 1.2)
    // draw unit
    g.setFont(digitalFont.deriveFont(Font.BOLD, fs / 4))
    val utext = unitText()
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, (w - utb.getWidth) / 2, cy + box / 2 + utb.getHeight * 2 - tb.getHeight * 1.2)
  }
}
