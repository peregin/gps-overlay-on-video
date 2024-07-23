package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

import java.awt._


class GradeGauge extends GaugePainter {
  var elevation: InputValue = _;

  lazy val dummy: InputValue = InputValue(8.5, MinMax.max(100))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = {
    input = sonda.grade
    elevation = sonda.elevation;
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

    // draw current grade
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h / 4).toFloat))
    val gradeText = f"${input.current}%2.1f%%"
    textWidthShadow(g, gradeText, w / 4, h / 2)

    // draw current grade
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h / 4).toFloat))
    val eleText = f"${elevation.current}%2.0f m"
    textWidthShadow(g, eleText, 0, h * 3 / 4)

  }
}
