package peregin.gpv.gui

import scala.swing.Component
import java.awt.{Font, Dimension}


abstract class GaugeComponent extends Component {

  preferredSize = new Dimension(75, 75)
  lazy val gaugeFont = new Font("Verdana", Font.PLAIN, 12)

  def polarX(cx: Double, r: Double, angle: Double): Int = (cx + r * math.cos(math.toRadians(angle))).toInt
  def polarY(cy: Double, r: Double, angle: Double): Int = (cy + r * math.sin(math.toRadians(angle))).toInt
}
