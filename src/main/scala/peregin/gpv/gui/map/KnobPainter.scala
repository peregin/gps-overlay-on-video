package peregin.gpv.gui.map

import java.awt.{BasicStroke, Color, Graphics2D}
import java.awt.geom.Point2D


trait KnobPainter {

  lazy val edge = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f)

  def paintKnob(g: Graphics2D, pt: Point2D, color: Color): Unit = paintKnob(g, pt.getX.toInt, pt.getY.toInt, color)

  def paintKnob(g: Graphics2D, x: Int, y: Int, color: Color): Unit = {
    g.setColor(Color.gray)
    g.fillOval(x - 3, y - 3, 10, 10)
    g.setColor(color)
    g.fillOval(x - 5, y - 5, 10, 10)
    g.setColor(Color.black)
    g.setStroke(edge)
    g.drawOval(x - 5, y - 5, 10, 10)
  }
}
