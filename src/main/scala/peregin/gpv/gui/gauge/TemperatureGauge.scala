package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, Sonda}

import java.awt.{Font, Graphics2D}


class TemperatureGauge extends GaugePainter {

  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    input = sonda.temperature
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    if (input != null) {
      super.paint(g, devHeight, w, h)

      g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))
      val textBounds = g.getFontMetrics().getStringBounds(" 99°C", g)
      if (input.current.isDefined) {
        // draw temperature
        val text = f"${input.current.get}% 2.0f°C"
        textWidthShadow(g, text, w - textBounds.getWidth, h / 2 - h / 8)
      }

      val x = (w - textBounds.getWidth - w / 8 - 5).toInt

      // draw the border
      drawShadowed(g, h, (g0) => {
        g.drawArc(x - h / 16, h / 8, h / 8, h / 8, 0, 180)
        g.drawLine(x - h / 16, h / 8 + h / 16, x - h / 16, h / 2)
        g.drawLine(x + h / 16 + 1, h / 8 + h / 16, x + h / 16 + 1, h / 2)
        g.drawArc(x - h / 8, h / 2 - h / 64, h / 4, h / 4, 60, -300)
      })
    }
  }
}
