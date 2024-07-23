package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

import java.awt._


class GradeGauge extends GaugePainter {

  lazy val dummy: InputValue = InputValue(8.5, MinMax.max(100))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = {
    input = sonda.grade
  }

  override def paint(g: Graphics2D, w: Int, h: Int): Unit = {
    super.paint(g, w, h)

    val box = math.min(w, h)
    val strokeWidth = box / 5

    val slopeXs = Array(0, w / 4, w / 4)
    val slopeYs = Array(h / 2, h / 4, h / 2)

    // draw a slope triangle
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    g.setColor(Color.black)
    g.fillPolygon(slopeXs, slopeYs, slopeXs.length);

    // draw the border
    g.setColor(Color.yellow)
    val borderStroke = new BasicStroke(math.max(2, strokeWidth / 10))
    g.setStroke(borderStroke)
    g.drawPolygon(slopeXs, slopeYs, slopeXs.length);

    // draw the the ticks and units
    g.setColor(Color.white)

    // draw current speed
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h / 4).toFloat))
    val text = f"${input.current}%2.1f%%"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, w / 4, h / 2)

  }
}
