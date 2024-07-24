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

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    val slopeXs = Array(0, w / 4, w / 4)
    val slopeYs = Array(h / 2, h / 4, h / 2)

    // draw a slope triangle
    drawShadowed(g, h, (g0) => {
      g0.drawPolygon(slopeXs, slopeYs, slopeXs.length);
    })

    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))

    // draw current grade
    val gradeText = f"${input.current}%2.1f%%"
    textWidthShadow(g, gradeText, w / 4 + 5, h / 2 - 10)

    // draw current grade
    val eleText = f"${elevation.current}% 4.0f m"
    textWidthShadow(g, eleText, 0, h * 3 / 4 + 10)

  }
}
