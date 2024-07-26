package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, Sonda}

import java.awt._
import java.time.{LocalDateTime, ZoneId}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField
import java.util.Locale


class DateTimeGauge extends GaugePainter {
  val PADDING: Int = 5;
  val TIME_FORMATTER: DateTimeFormatter = (new DateTimeFormatterBuilder).appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter(Locale.ROOT)

  var localTime: LocalDateTime = _;
  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    localTime = LocalDateTime.ofInstant(sonda.time.toDate.toInstant, ZoneId.systemDefault())
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    // draw current time
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))
    val dateText = DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
    val timeText = TIME_FORMATTER.format(localTime)

    val dateBounds = g.getFontMetrics().getStringBounds("9999-99-99 ", g)
    val timeBounds = g.getFontMetrics().getStringBounds("99:99:99 ", g)

    val width: Double = math.max(dateBounds.getWidth, timeBounds.getWidth)
//    backgroundSemiTransparent(g, 0, h / 4 - dateBounds.getHeight, width + 2 * PADDING, h * 2 / 4 +1 - (h / 4 - dateBounds.getHeight) + PADDING)
//    g.setColor(Color.yellow)
//    g.drawString(dateText, PADDING, h / 4)
//    g.drawString(timeText, PADDING, h * 2 /4 + 1)
    textWidthShadow(g, dateText, w - dateBounds.getWidth, dateBounds.getHeight)
    textWidthShadow(g, timeText, w - timeBounds.getWidth, dateBounds.getHeight + timeBounds.getHeight)
  }
}
