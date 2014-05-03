package peregin.gpv.gui.gauge

import scala.swing.{Graphics2D, Component}


// used for testing
class GaugeComponent extends Component {
  this: GaugePainter =>

  debug = true

  override def preferredSize = desiredSize

  override def paint(g: Graphics2D) {
    paint(g, peer.getWidth, peer.getHeight)
  }
}
