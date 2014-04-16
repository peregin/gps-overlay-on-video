package peregin.gpv.gui.gauge

import peregin.gpv.gui.GaugeComponent
import scala.swing.Graphics2D
import java.awt.{RenderingHints, BasicStroke, Color}
import java.awt.geom.Arc2D


class SpeedGauge extends GaugeComponent {

  override def paint(g: Graphics2D) = {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE)

    val w = peer.getWidth
    val h = peer.getHeight
    val box = math.min(w, h)
    val strokeWidth = box / 5
    var d = box - strokeWidth * 1.5

    g.setColor(Color.yellow)
    g.drawRoundRect(1, 1, w - 2, h - 2, 5, 5)

    // draw a thick open arc
    var x = (w - d) / 2
    var y = (h - d) / 2
    val start = -45
    val extent = 270
    var arc = new Arc2D.Double(x, y, d, d, start, extent, Arc2D.OPEN)
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    g.setColor(Color.black)
    g.draw(arc)

    // draw the border
    d = box - strokeWidth / 2
    x = (w - d) / 2
    y = (h - d) / 2
    arc = new Arc2D.Double(x, y, d, d, start, extent, Arc2D.OPEN)
    g.setColor(Color.white)
    val borderStroke = new BasicStroke(math.max(2, strokeWidth / 10))
    g.setStroke(borderStroke)
    g.draw(arc)

    // draw the the ticks and units
    val r = d / 2 // the radius of the circle
    val cx = w / 2
    val cy = h / 2
    val ticks = 6 * 10
    val longTickLength = math.max(2, r / 10)
    val smallTickLength = math.max(1, longTickLength / 2)
    val tickStroke = new BasicStroke(math.max(1, strokeWidth / 20))
    g.setFont(gaugeFont.deriveFont((longTickLength + 2).toFloat))
    for (t <- 0 to ticks) {
      val angle = -start - t * extent / ticks
      val tickLength = if (t % 10 == 0) {
        g.setStroke(borderStroke)
        val text = s"${ticks - t}"
        val tb = g.getFontMetrics.getStringBounds(text, g)
        val tw = tb.getWidth / 2
        val th = tb.getHeight / 2
        val tp = r - longTickLength - tw - 2
        g.drawString(text, polarX(cx, tp, angle) - tw.toInt, polarY(cy, tp, angle) + th.toInt)
        longTickLength
      } else {
        g.setStroke(tickStroke)
        smallTickLength
      }
      g.drawLine(polarX(cx, r, angle), polarY(cy, r, angle), polarX(cx, r - tickLength, angle), polarY(cy, r - tickLength, angle))
    }

    // draw pointer
    g.setColor(Color.black)
    var cr = (longTickLength / 2).toInt + 1
    g.fillOval(cx - cr, cy - cr, 2 * cr, 2 * cr)
    g.setColor(Color.yellow)
    cr -= 1
    g.fillOval(cx - cr, cy - cr, 2 * cr, 2 * cr)
  }
}
