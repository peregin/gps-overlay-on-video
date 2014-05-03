package peregin.gpv.gui.gauge

import java.awt._
import peregin.gpv.model.MinMax
import peregin.gpv.model.InputValue
import MinMax.RoundedDouble


trait LinearElevationGauge extends GaugePainter {

  lazy val dummy = InputValue(728, MinMax(592, 1718))
  override def defaultInput = dummy

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

    val box = math.min(w, h)
    val boxWidth = box / 8
    val boxHeight = (box / 1.2).toInt
    val strokeWidth = box / 40

    // draw altimeter box
    g.setColor(Color.black)
    val cx = (w - boxWidth) / 2
    val cy = (h - boxHeight) / 2
    g.fillRect(cx, cy, boxWidth, boxHeight)

    // draw current altitude
    g.setColor(colorBasedOnInput)
    val ticks = input.boundary.hundredths
    val vy = ((input.current - input.boundary.min.roundDownToHundredth) * boxHeight / ticks).toInt
    g.fillRect(cx + boxWidth / 4, cy + boxHeight - vy, (boxWidth / 1.8).toInt, vy)

    // draw the border of the box
    g.setColor(Color.white)
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f))
    g.drawRect(cx, (h - boxHeight) / 2, boxWidth, boxHeight)

    // draw the the ticks and units
    g.setColor(Color.white)
    val longTickLength = math.max(2, strokeWidth)
    val tickStroke = new BasicStroke(math.max(1, strokeWidth / 20))
    g.setFont(gaugeFont.deriveFont((longTickLength + 1).toFloat))
    g.setStroke(tickStroke)
    for (t <- 0 to ticks by 100) {
      val y = cy + (t * boxHeight / ticks)
      g.drawLine(cx, y, cx + boxWidth / 4, y)
      val text = s"${ticks - t + input.boundary.min.roundDownToHundredth}"
      val tb = g.getFontMetrics.getStringBounds(text, g)
      val th = tb.getHeight / 2
      g.drawString(text, cx + boxWidth / 3, y + th.toInt)
    }

    // draw current altitude
    val ry = h / 2
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (boxWidth).toFloat))
    val text = f"${input.current}%1.0f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, (w - tb.getWidth) / 2, ry + tb.getHeight / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (strokeWidth).toFloat))
    val utext = "m"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, (w - utb.getWidth) / 2, ry + tb.getHeight / 2 + utb.getHeight)
  }
}
