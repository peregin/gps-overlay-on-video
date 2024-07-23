package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

import java.awt._


class TemperatureGauge extends GaugePainter {

  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    input = sonda.temperature.orNull
  }

  override def paint(g: Graphics2D, w: Int, h: Int): Unit = {
    if (input != null) {
      super.paint(g, w, h)

      val box = math.min(w, h)
      val strokeWidth = box / 5

      // draw the border
      g.setColor(Color.yellow)
      val borderStroke = new BasicStroke(math.max(2, strokeWidth / 10))
      g.setStroke(borderStroke)
      g.drawArc(w / 4, h / 8, w / 8, h / 8, 0, 180)
      g.drawLine(w / 4, h / 8 + h / 16, w / 4, h / 2)
      g.drawLine(w / 4 + w / 8, h / 8 + h / 16, w / 4 + w / 8, h / 2)
      g.drawArc(w / 4 - w / 16, h / 2, w / 4, h / 4, 60, -300)

      // draw the the ticks and units
      g.setColor(Color.white)

      // draw current speed
      g.setFont(gaugeFont.deriveFont(Font.BOLD, (h / 4).toFloat))
      val text = f"${input.current}%2.0f℃"
      val tb = g.getFontMetrics.getStringBounds(text, g)
      textWidthShadow(g, text, w / 4 + w /8 + w / 32, h / 2 - h / 8)
    }
  }
}
