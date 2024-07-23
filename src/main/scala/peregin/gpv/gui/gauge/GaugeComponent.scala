package peregin.gpv.gui.gauge

import scala.swing.{Component, Dimension, Graphics2D}


// used for testing
class GaugeComponent(gaugePainter0: GaugePainter) extends Component {

  def gaugePainter: GaugePainter = gaugePainter0

  override def preferredSize: Dimension = gaugePainter.desiredSize

  override def paint(g: Graphics2D): Unit = {
    // set transparency for testing
    // g.setComposite(AlphaComposite.SrcOver.derive(0.5f))
    gaugePainter.paint(g, peer.getWidth, peer.getHeight)
  }
}
