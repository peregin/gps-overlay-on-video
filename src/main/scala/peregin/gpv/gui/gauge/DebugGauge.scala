package peregin.gpv.gui.gauge

import peregin.gpv.model.{Sonda, InputValue}
import java.awt._
import org.joda.time.{Duration, DateTime}
import peregin.gpv.util.Io
import org.joda.time.format.PeriodFormatterBuilder


trait DebugGauge extends GaugePainter {

  lazy val bugImage = Io.loadImage("images/bug.png")
  lazy val formatter = new PeriodFormatterBuilder()
    .appendDays()
    .appendSuffix("d")
    .appendHours()
    .appendSuffix("h")
    .appendMinutes()
    .appendSuffix("m")
    .appendSeconds()
    .appendSuffix("s")
    .toFormatter

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
    g.drawImage(bugImage, px, py, null)

    // draw current time
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 14).toFloat))
    val fh = g.getFontMetrics.getHeight
    val tx = px + d + r
    val ty = cy - fh
    // start drawing debug info
    var text = current.map(_.time).getOrElse(DateTime.now()).toString("HH:mm:ss.SSS")
    textWidthShadow(g, s"GPS Time: $text", tx, ty, Color.white)
    text = millis2Period(current.map(_.elapsedTime.current.toLong))
    textWidthShadow(g, s"GPS Elapsed: $text", tx, ty + fh, Color.white)
    text = millis2Period(current.map(_.videoProgress))
    textWidthShadow(g, s"VID Elapsed: $text", tx, ty + 2 * fh, Color.white)
  }

  def millis2Period(elapsed: Option[Long]) = formatter.print(new Duration(elapsed.getOrElse(0L)).toPeriod)
}
