package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, Sonda}

import java.awt._
import java.time.Instant
import java.time.temporal.ChronoUnit


class CaptionGauge extends ChartPainter {
  val PADDING: Int = 5;

  var defaultTransparency: Float = 1.0f
  var time: Instant = _

  override def defaultInput: InputValue = null

  override def sample(sonda: Sonda): Unit = {
    time = sonda.time.toDate.toInstant
  }

  override def transparency_=(transparencyInPercentage: Double): Unit = {
    super.transparency_=(transparencyInPercentage)
    defaultTransparency = (transparencyInPercentage / 100.0f).toFloat
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    val messages = telemetry.captionsForTime(time)
    if (messages.isEmpty) {
      return
    }

    var y = 0.0
    g.setFont(gaugeFont.deriveFont(Font.PLAIN, h.toFloat))

    for (message <- messages) {
      val progress = message.start.get.until(time, ChronoUnit.MILLIS)
      val transparency = math.min(progress, (message.duration * 1000).toLong - progress)
      var color: Color = Color.green
      if (transparency < 1000) {
        color = new Color(color.getRed, color.getGreen, color.getBlue, (transparency * (256 / 1000.0f)).toInt)
      }
      for (text <- message.text.split('\n')) {
        val bounds = g.getFontMetrics().getStringBounds(text, g)
        y += bounds.getHeight
        textWidthShadow(g, text, (w - bounds.getWidth) / 2, y, c = color)
      }
      y += PADDING
    }
  }

}
