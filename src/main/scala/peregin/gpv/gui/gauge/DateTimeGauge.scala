package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, Sonda}

import java.awt._
import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter


class DateTimeGauge extends GaugePainter {

  var localTime: LocalDateTime = _;
  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    localTime = LocalDateTime.ofInstant(sonda.time.toDate.toInstant, ZoneId.systemDefault())
  }

  override def paint(g: Graphics2D, w: Int, h: Int): Unit = {
    super.paint(g, w, h)

    // draw current time
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))
    val dateText = f"Date: ${DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)}%s"
    val timeText = f"Time: ${DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)}%s"
    textOnSemiTransparent(g, dateText, 0, h / 4)
    textOnSemiTransparent(g, timeText, 0, h * 2 /4 + 1)
  }
}
