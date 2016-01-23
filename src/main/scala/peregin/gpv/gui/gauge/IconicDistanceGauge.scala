package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, MinMax, InputValue}
import java.awt._
import java.awt.geom.{Ellipse2D, Rectangle2D, Area}
import peregin.gpv.util.Trigo._
import peregin.gpv.util.UnitConverter


trait IconicDistanceGauge extends GaugePainter {

  lazy val dummy = InputValue(80.21, MinMax(0, 123.4))
  override def defaultInput = dummy
  override def sample(sonda: Sonda) {input = sonda.distance}

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

    val box = math.min(w, h)
    val cy = h / 2

    // draw a pinpoint icon
    g.setColor(Color.white)
    val d = box / 7
    val r = d / 2
    val ph = d + r
    val px = 10
    val py = ((h - 10) - r) / 2
    // http://en.wikipedia.org/wiki/List_of_trigonometric_identities
    val l = leg(d, r)
    val oe = math.asin(r.toDouble / d) - math.Pi / 2
    val ty = polarY(py + ph, l, oe.toDegrees)
    val tx = polarX(px + r, l, oe.toDegrees)
    val poly = new Polygon
    poly.addPoint(px + r, py + ph)
    poly.addPoint(tx, ty)
    poly.addPoint(2 * (px + r) - tx, ty)
    val pp = new Area(poly)
    pp.add(new Area(new Ellipse2D.Double(px, py, d, d)))
    g.draw(pp)
    g.fill(pp)

    // fill the pinpoint according to the current distance
    val pointerHeight = (input.current - input.boundary.min) * ph / input.boundary.diff
    val a1 = new Area(new Rectangle2D.Double(px, py + ph - pointerHeight, d, ph))
    val a2 = new Area(poly)
    a2.add(new Area(new Ellipse2D.Double(px, py, d, d)))
    a1.intersect(a2)
    g.setColor(Color.gray)
    g.fill(a1)

    // draw current distance with one decimal place
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 4).toFloat))
    val text = f"${UnitConverter.distance(input.current, units)}%2.1f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, px + (w - tb.getWidth) / 2, (h + tb.getHeight) / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 12).toFloat))
    val utext = UnitConverter.distanceUnits(units)
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, px + (w - utb.getWidth) / 2, cy + utb.getHeight * 2.1)
  }
}
