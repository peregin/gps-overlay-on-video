package peregin.gpv.gui.gauge

import peregin.gpv.model.MinMax
import java.awt._
import peregin.gpv.model.InputValue
import java.awt.geom.{Rectangle2D, Area}


class HeartRateGauge extends GaugeComponent {

  val dummy = InputValue(89, MinMax(62, 171))
  override def defaultInput = dummy

  override def paint(g: Graphics2D) = {
    super.paint(g)

    val w = peer.getWidth
    val h = peer.getHeight
    val box = math.min(w, h)
    val cy = h / 2

    // draw hear icon
    g.setColor(Color.white)
    val px = 10
    val py = (h - 10) / 2
    val d = box / 5
    g.fillOval(px, py, d / 2, d / 2)
    g.fillOval(px + d / 2, py, d / 2, d / 2)
    val tri = new Polygon
    tri.addPoint(px, py + d / 4)
    tri.addPoint(px + d, py + d / 4)
    tri.addPoint(px + d / 2, py + d)
    g.setColor(Color.black)
    g.fill(tri)

    // draw current value
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 4).toFloat))
    val text = f"${input.current}%2.0f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, px + (w - tb.getWidth) / 2, (h + tb.getHeight) / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 12).toFloat))
    val utext = "bpm"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, px + (w - utb.getWidth) / 2, cy + utb.getHeight * 2.2)
  }
}
