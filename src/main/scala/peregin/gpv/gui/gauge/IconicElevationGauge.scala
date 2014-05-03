package peregin.gpv.gui.gauge

import peregin.gpv.model.MinMax
import java.awt._
import peregin.gpv.model.InputValue
import java.awt.geom.{Rectangle2D, Area}


trait IconicElevationGauge extends GaugePainter {

  lazy val dummy = InputValue(689, MinMax(432, 1252))
  override def defaultInput = dummy

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

    val box = math.min(w, h)
    val cy = h / 2

    // draw mountain icon
    val strokeWidth = math.max(2, box / 40)
    g.setColor(Color.white)
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    val px = 10
    val py = (h - 10) / 2
    val pw = box / 5
    val ph = pw
    val poly = new Polygon
    poly.addPoint(px, py + ph)
    poly.addPoint(px + pw, py + ph)
    poly.addPoint(px + pw * 3 / 4, py + ph / 3) // little peak
    poly.addPoint(px + pw / 2, py + ph * 3 / 5 )
    poly.addPoint(px + pw / 4, py) // big peak
    g.draw(poly)
    g.fill(poly)

    // fill the polygon according to the current altitude
    val pointerHeight = (input.current - input.boundary.min) * ph / input.boundary.diff
    val rect = new Rectangle2D.Double(px, py + ph - pointerHeight, pw, py + ph)
    val a1 = new Area(rect)
    val a2 = new Area(poly)
    a1.intersect(a2)
    g.setColor(Color.gray)
    g.fill(a1)

    // draw current elevation
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 4).toFloat))
    val text = f"${input.current}%2.0f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, px + (w - tb.getWidth) / 2, (h + tb.getHeight) / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 12).toFloat))
    val utext = "m"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, px + (w - utb.getWidth) / 2, cy + utb.getHeight * 2.1)
  }
}
