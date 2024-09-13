package peregin.gpv.gui.gauge

import scala.swing._
import java.awt.{BasicStroke, Color, Dimension, Font, RenderingHints}
import peregin.gpv.model.{InputValue, Sonda}

import java.util.function.Consumer


trait GaugePainter {

  lazy val gaugeFont = new Font("Verdana", Font.PLAIN, 12)
  private var currentInput: InputValue = InputValue.empty
  private var debugging = false
  private var displayUnits: String = ""

  def desiredSize = new Dimension(75, 75)

  def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE)

    if (debugging) {
      g.setColor(Color.yellow)
      g.drawRoundRect(1, 1, w - 2, h - 2, 5, 5)
    }
  }

  final def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int, sonda: Sonda): Unit = {
    sample(sonda)
    paint(g, devHeight, w, h)
  }

  // each implementation should extract the desired input data from the sonda
  // e.g. speed gauge extracts speed input, etc.
  def sample(sonda: Sonda): Unit

  // each implementation should provide the default values used for testing or to show a sample in the gauges' list
  def defaultInput: InputValue

  def input: InputValue = currentInput
  def input_= (v: InputValue): Unit = currentInput = v

  def debug: Boolean = debugging
  def debug_= (v: Boolean): Unit = debugging = v

  def units: String = displayUnits
  def units_= (v: String): Unit = displayUnits = v

  def lineWidth(g: Graphics2D, devHeight: Int): Int = {
    math.ceil(devHeight / 360.0).toInt
  }

  def shadowWidth(g: Graphics2D, devHeight: Int): Int = {
    return 1 + lineWidth(g, devHeight) * 2
  }

  def drawShadowed(g: Graphics2D, devHeight: Int, consumer: Consumer[Graphics2D]): Unit = {
    // For 480, we want 2 pixels, for standard 1080, we want 3 pixels
    g.setColor(new Color(0, 0, 0, 128))
    g.setStroke(new BasicStroke(shadowWidth(g, devHeight).toFloat))
    consumer.accept(g)
    g.setColor(Color.yellow)
    g.setStroke(new BasicStroke(lineWidth(g, devHeight).toFloat))
    consumer.accept(g)
  }

  def textWidthShadow(g: Graphics2D, text: String, x: Double, y: Double, c: Color = Color.yellow): Unit = {
    val ix = x.toInt
    val iy = y.toInt
    g.setColor(new Color(0, 0, 0, c.getAlpha))
    g.drawString(text, ix + 1, iy + 1)
    g.setColor(c)
    g.drawString(text, ix, iy)
  }

  def textOnSemiTransparent(g: Graphics2D, text: String, x: Double, y: Double, c: Color = Color.yellow): Unit = {
    val ix = x.toInt
    val iy = y.toInt
    g.setColor(new Color(0, 0, 0, 128))
    val rect = g.getFontMetrics().getStringBounds(text, g).getBounds
    g.fillRect((x + rect.getX).toInt, (y + rect.getY).toInt, rect.width.toInt, rect.height.toInt)
    g.setColor(c)
    g.drawString(text, ix, iy)
  }

  def backgroundSemiTransparent(g: Graphics2D, x: Double, y: Double, w: Double, h: Double): Unit = {
    g.setColor(new Color(0, 0, 0, 128))
    g.fillRect(x.toInt, y.toInt, w.toInt, h.toInt)
  }

  def colorBasedOnInput: Color = input match {
    case _ if input.isInTop(10) => Color.red
    case _ if input.isInTop(20) => Color.yellow
    case _ if input.isInTop(50) => Color.green
    case _ => Color.gray
  }
}
