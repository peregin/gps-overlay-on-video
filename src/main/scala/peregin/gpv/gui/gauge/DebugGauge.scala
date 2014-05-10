package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, InputValue}
import java.awt._
import org.joda.time.DateTime


trait DebugGauge extends GaugePainter {

  var current: Option[Sonda] = None

  override def defaultInput = InputValue.zero


  override def sample(sonda: Sonda) {
    current = Some(sonda)
  }

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

    val box = math.min(w, h)
    val cy = h / 2

    val d = box / 7
    val r = d / 2
    val px = 10
    val py = cy - r
    // draw a clock icon
    g.setColor(Color.white)
    g.setStroke(new BasicStroke(math.max(1, d / 10), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    g.drawOval(px, py, d, d)

    // draw current time
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 14).toFloat))
    val fh = g.getFontMetrics.getHeight
    val tx = px + d + r
    val ty = cy - fh
    // start drawing debug info
    var text = current.map(_.time).getOrElse(DateTime.now()).toString("hh:mm:ss.SSS")
    textWidthShadow(g, text, tx, ty)
    text = "another line"
    textWidthShadow(g, text, tx, ty + fh)
    text = "and another"
    textWidthShadow(g, text, tx, ty + 2 * fh)
  }
}
