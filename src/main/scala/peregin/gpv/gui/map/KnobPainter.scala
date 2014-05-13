package peregin.gpv.gui.map

import java.awt.{Color, Graphics2D}
import java.awt.geom.Point2D


trait KnobPainter {

  def paintKnob(g: Graphics2D, pt: Point2D, color: Color): Unit = paintKnob(g, pt.getX.toInt, pt.getY.toInt, color)

  def paintKnob(g: Graphics2D, x: Int, y: Int, color: Color) {
    g.setColor(color)
    g.fillOval(x - 5, y - 5, 10, 10)
  }
}
