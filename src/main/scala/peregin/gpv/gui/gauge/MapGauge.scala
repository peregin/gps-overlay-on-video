package peregin.gpv.gui.gauge

import org.jdesktop.swingx.mapviewer.GeoPosition
import peregin.gpv.format.GpsFormatter
import peregin.gpv.model.{InputValue, MinMax, Sonda, Telemetry}

import java.awt.{Color, Graphics2D}


class MapGauge extends ChartPainter {
  val PADDING: Int = 5;

  var location: GeoPosition = _;

  var longitudeBoundary: MinMax = _;
  var latitudeBoundary: MinMax = _;
  var scaleLongitude: Double = _;

  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    location = sonda.location
  }

  override def telemetry_=(telemetry: Telemetry): Unit = {
    super.telemetry_=(telemetry)

    latitudeBoundary = telemetry.latitudeBoundary
    longitudeBoundary = telemetry.longitudeBoundary

    // In case we sail in Pacific Ocean, swap the min/max to range from Asia to America
    if (longitudeBoundary.min - longitudeBoundary.max < -180) {
      longitudeBoundary = MinMax(longitudeBoundary.max, longitudeBoundary.min)
    }

    // longitude circumference changes based on roughly cos(latitude), we want to scale to make the map scale based on distance:
    val majorLatitude = if ((latitudeBoundary.min > 0) != (latitudeBoundary.max > 0)) 0 else Math.min(Math.abs(latitudeBoundary.min), Math.abs(latitudeBoundary.max));
    scaleLongitude = Math.cos(Math.toRadians(majorLatitude))
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    if (latitudeBoundary == null || longitudeBoundary == null) {
      return
    }

    val scale = screenScale(w, h)
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

  private def screenScale(w: Int, h: Int): Double = {
    val scaleX = (w - 2 * PADDING) / Math.max(GpsFormatter.subtractLongitude(longitudeBoundary.max, longitudeBoundary.min) * scaleLongitude, 0.001)
    val scaleY = (h - 2 * PADDING) / Math.max(latitudeBoundary.max - latitudeBoundary.min, 0.001)
    math.min(scaleX, scaleY)
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

  private[gauge] def longitudeToScreen(lon: Double, scale: Double): Int = {
    return (GpsFormatter.subtractLongitude(lon, longitudeBoundary.min) * scaleLongitude * scale).toInt;
  }

  private[gauge] def latitudeToScreen(lat: Double, scale: Double): Int = {
    return ((lat - latitudeBoundary.min) * scale).toInt;
  }
}
