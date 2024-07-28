package peregin.gpv.gui.gauge

import org.jdesktop.swingx.mapviewer.GeoPosition
import peregin.gpv.format.GpsFormatter
import peregin.gpv.model.{InputValue, MinMax, Sonda, Telemetry}

import java.awt.{BasicStroke, Color, Graphics2D}
import java.util


class MapGauge extends ChartPainter {
  val PADDING: Int = 5;

  var location: GeoPosition = _;

  var longitudeBoundary: MinMax = _;
  var latitudeBoundary: MinMax = _;

  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    location = sonda.location
  }

  override def telemetry_=(telemetry: Telemetry): Unit = {
    super.telemetry_=(telemetry)

    latitudeBoundary = telemetry.latitudeBoundary
    longitudeBoundary = telemetry.longitudeBoundary
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    if (latitudeBoundary == null || longitudeBoundary == null) {
      return
    }
    val scaleX = (w - 2 * PADDING) / GpsFormatter.subtractLongitude(longitudeBoundary.max, longitudeBoundary.min)
    val scaleY = (h - 2 * PADDING) / (latitudeBoundary.max - latitudeBoundary.min)
    val scale = math.min(scaleX, scaleY)

    val yBase = PADDING + ((latitudeBoundary.max - latitudeBoundary.min) * scale).toInt

    drawMap(g, w, h, scale, yBase)

    val radius = (shadowWidth(g, devHeight) + 3) / 2
    g.setColor(Color.red)
    g.fillOval(
      PADDING + longitudeToScreen(location.getLongitude, scale) - radius,
      (yBase - latitudeToScreen(location.getLatitude, scale)) - radius,
      radius * 2,
      radius * 2,
    )
  }

  private def drawMap(g: Graphics2D, w: Int, h: Int, scale: Double, yBase: Int): Unit = {
    // Collect points every 5 seconds, 100 at least but track size at max:
    val sampling = math.min(math.max(telemetry.track.size / 5, 100), telemetry.track.size)
    val xPoints: Array[Int] = Array.fill(sampling + 1){0}
    val yPoints: Array[Int] = Array.fill(sampling + 1){0}
    for (i <- 0 to sampling) {
      val current = telemetry.pointForProgress(100.0 * i / sampling)
      xPoints(i) = PADDING + longitudeToScreen(current.position.getLongitude, scale)
      yPoints(i) = yBase - latitudeToScreen(current.position.getLatitude, scale)
    }
    drawShadowed(g, h, (g0) => {
      g0.drawPolyline(xPoints, yPoints, xPoints.length)
    })
  }

  private def longitudeToScreen(lon: Double, scale: Double): Int = {
    return (GpsFormatter.subtractLongitude(lon, longitudeBoundary.min) * scale).toInt;
  }

  private def latitudeToScreen(lat: Double, scale: Double): Int = {
    return ((lat - latitudeBoundary.min) * scale).toInt;
  }
}
