package peregin.gpv.gui.gauge

import peregin.gpv.format.TimeFormatter
import peregin.gpv.model.{InputValue, Sonda, Telemetry}
import peregin.gpv.util.UnitConverter

import java.awt._
import java.time.temporal.ChronoUnit
import java.time.Instant


class DistanceAndDurationGauge extends ChartPainter {
  val PADDING: Int = 5;

  var startTime: Instant = _;
  var currentTime: Instant = _;

  var currentDistance: Double = _;

  override def defaultInput: InputValue = null

  override def telemetry_=(telemetry: Telemetry): Unit = {
    super.telemetry_=(telemetry)

    startTime = telemetry.minTime.toDate.toInstant
  }

  override def sample(sonda: Sonda): Unit = {
    currentTime = sonda.time.toDate.toInstant
    currentDistance = sonda.distance.current.get
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    if (startTime == null || currentTime == null || currentDistance == null) {
      return;
    }
    // draw current time
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 8).toFloat))
    val distanceText = f"${UnitConverter.distance(currentDistance, units)}%1.1f ${UnitConverter.distanceUnits(units)}%s"
    val timeText = TimeFormatter.formatTime(startTime.until(currentTime, ChronoUnit.SECONDS))

    val distanceBounds = g.getFontMetrics().getStringBounds(distanceText + " ", g)
    val timeBounds = g.getFontMetrics().getStringBounds(timeText + " ", g)

    textWidthShadow(g, distanceText, w - distanceBounds.getWidth, distanceBounds.getHeight)
    textWidthShadow(g, timeText, w - timeBounds.getWidth, distanceBounds.getHeight + timeBounds.getHeight)
  }
}
