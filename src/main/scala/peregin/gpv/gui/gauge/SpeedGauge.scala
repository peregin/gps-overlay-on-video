package peregin.gpv.gui.gauge

import peregin.gpv.gui.GaugeComponent
import scala.swing.Graphics2D
import java.awt.{RenderingHints, BasicStroke, Font, Color}
import scala.swing.Font
import java.awt.geom.Arc2D


class SpeedGauge extends GaugeComponent {

  val sf = new Font("Dialog", Font.BOLD, 12)

  override def paint(g: Graphics2D) = {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    val w = peer.getWidth
    val h = peer.getHeight
    val b = math.min(w, h)
    val strokeWidth = b / 5
    var d = b - strokeWidth * 1.5

    g.setColor(Color.yellow)
    g.drawRoundRect(1, 1, w - 2, h - 2, 5, 5)

    // draw a thick open arc
    var x = (w - d) / 2
    var y = (h - d) / 2
    val start = -45
    val extent = 270
    var arc = new Arc2D.Double(x, y, d, d, start, extent, Arc2D.OPEN)
    g.setStroke(new BasicStroke(strokeWidth))
    g.setColor(Color.black)
    g.draw(arc)

    // draw the border
    d = b - strokeWidth / 2
    x = (w - d) / 2
    y = (h - d) / 2
    arc = new Arc2D.Double(x, y, d, d, start, extent, Arc2D.OPEN)
    g.setColor(Color.white)
    g.setStroke(new BasicStroke(2))
    g.draw(arc)

    // draw the marks
    val r = d / 2 // the radius of the circle
    val cx = w / 2
    val cy = h / 2
    val ticks = 6
    for (t <- 0 to ticks) {
      val angle = -start - t * extent / ticks
      g.drawLine(polarX(cx, r, angle), polarY(cy, r, angle), polarX(cx, r - 4, angle), polarY(cy, r - 4, angle))
    }
  }
  
  def polarX(cx: Double, r: Double, angle: Double): Int = (cx + r * math.cos(math.toRadians(angle))).toInt
  def polarY(cy: Double, r: Double, angle: Double): Int = (cy + r * math.sin(math.toRadians(angle))).toInt
}
