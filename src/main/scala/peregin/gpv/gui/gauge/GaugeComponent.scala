package peregin.gpv.gui.gauge

import scala.swing._
import java.awt.{Color, Font, Dimension}
import peregin.gpv.model.InputValue
import peregin.gpv.model.InputValue
import scala.Some
import scala.swing.Color


abstract class GaugeComponent extends Component {

  preferredSize = new Dimension(75, 75)
  lazy val gaugeFont = new Font("Verdana", Font.PLAIN, 12)
  private var currentInput: Option[InputValue] = None

  def defaultInput: InputValue
  def input = currentInput.getOrElse(defaultInput)
  def input_= (v: InputValue) {
    currentInput = Some(v)
    repaint()
  }

  def polarX(cx: Double, r: Double, angle: Double): Int = (cx + r * math.cos(math.toRadians(angle))).toInt
  def polarY(cy: Double, r: Double, angle: Double): Int = (cy + r * math.sin(math.toRadians(angle))).toInt

  def textWidthShadow(g: Graphics2D, text: String, x: Double, y: Double) {
    val ix = x.toInt
    val iy = y.toInt
    g.setColor(Color.black)
    g.drawString(text, ix + 1, iy + 1)
    g.setColor(Color.yellow)
    g.drawString(text, ix, iy)
  }
}
