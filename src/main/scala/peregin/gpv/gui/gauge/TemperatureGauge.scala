package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

import java.awt._


class TemperatureGauge extends GaugePainter {

  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    input = sonda.temperature.orNull
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    if (input != null) {
      super.paint(g, devHeight, w, h)

      val box = math.min(w, h)

      // draw the border
      drawShadowed(g, h, (g0) => {
        g.drawArc(w / 4, h / 8, w / 8, h / 8, 0, 180)
        g.drawLine(w / 4, h / 8 + h / 16, w / 4, h / 2)
        g.drawLine(w / 4 + w / 8, h / 8 + h / 16, w / 4 + w / 8, h / 2)
        g.drawArc(w / 4 - w / 16, h / 2, w / 4, h / 4, 60, -300)
      })

      // draw current speed
      g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))
      val text = f"${input.current}% 2.0f℃"
      textWidthShadow(g, text, w / 4 + w /8 + w / 32, h / 2 - h / 8)
    }
  }
}
