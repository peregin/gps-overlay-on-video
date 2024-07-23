package peregin.gpv.gui.gauge

import java.awt._
import java.awt.geom.Arc2D
import peregin.gpv.model.{Sonda, MinMax, InputValue}
import peregin.gpv.util.Trigo._


class CadenceGauge extends GaugePainter {

  lazy val dummy = InputValue(81, MinMax(0, 123))
  override def defaultInput = dummy
  override def sample(sonda: Sonda): Unit = {sonda.cadence.foreach(input = _)}

  override def paint(g: Graphics2D, w: Int, h: Int): Unit = {
    super.paint(g, w, h)

    val box = math.min(w, h)
    val strokeWidth = box / 5
    var dia = box - strokeWidth * 1.5

    // draw a thick open arc
    var x = (w - dia) / 2
    var y = (h - dia) / 2
    val start = 65
    val extent = 160
    var arc = new Arc2D.Double(x, y, dia, dia, start, extent, Arc2D.OPEN)
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    g.setColor(Color.black)
    g.draw(arc)

    // draw the border
    dia = box - strokeWidth / 2
    x = (w - dia) / 2
    y = (h - dia) / 2
    arc = new Arc2D.Double(x, y, dia, dia, start, extent, Arc2D.OPEN)
    g.setColor(Color.white)
    val borderStroke = new BasicStroke(math.max(2, strokeWidth / 10))
    g.setStroke(borderStroke)
    g.draw(arc)

    // draw moving arc, showing the actual cadence
    g.setColor(colorBasedOnInput)
    val pdia = box - strokeWidth * 1.5
    val px = (w - pdia) / 2
    val py = (h - pdia) / 2
    val pointerAngle = -135
    val pointerLength = -input.current * extent / input.boundary.tenths
    arc = new Arc2D.Double(px, py, pdia, pdia, pointerAngle, pointerLength, Arc2D.OPEN)
    g.setStroke(new BasicStroke(math.max(2, strokeWidth / 4), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    g.draw(arc)

    // draw the the ticks and units
    g.setColor(Color.white)
    val r = dia / 2 // the radius of the circle
    val cx = w / 2
    val cy = h / 2
    val ticks = input.boundary.tenths
    val longTickLength = math.max(2, r / 10)
    g.setFont(gaugeFont.deriveFont((longTickLength + 2).toFloat))
    for (t <- 0 to ticks) {
      val angle = -start - t * extent / ticks
      if (t % ticks == 0) {
        g.setStroke(borderStroke)
        val tickLength = strokeWidth
        g.drawLine(polarX(cx, r, angle), polarY(cy, r, angle), polarX(cx, r - tickLength, angle), polarY(cy, r - tickLength, angle))
      }
      val step = if (ticks > 150) 40 else 20
      if (t % step == 0 && t > 0 && t < ticks) {
        val text = s"${ticks - t}"
        val tb = g.getFontMetrics.getStringBounds(text, g)
        val tw = tb.getWidth / 2
        val th = tb.getHeight / 2
        val tp = r - longTickLength - tw - 2
        textWidthShadow(g, text, polarX(cx, tp, angle) - tw.toInt, polarY(cy, tp, angle) + th.toInt, Color.white)
      }
    }

    // draw interior border
    dia = box - strokeWidth * 2.5
    x = (w - dia) / 2
    y = (h - dia) / 2
    g.setColor(Color.white)
    arc = new Arc2D.Double(x, y, dia, dia, start, extent, Arc2D.OPEN)
    g.draw(arc)

    // draw current speed
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (longTickLength * 6).toFloat))
    val text = f"${input.current}%2.0f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, (w - tb.getWidth) / 2, cy + box / 2 - tb.getHeight * 1.2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (longTickLength * 2).toFloat))
    val utext = "rpm"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, (w - utb.getWidth) / 2, cy + box / 2 - utb.getHeight * 2.5)
  }
}
