package peregin.gpv.gui.gauge

import org.jdesktop.swingx.mapviewer.GeoPosition
import peregin.gpv.format.GpsFormatter
import peregin.gpv.model.{InputValue, MinMax, Sonda, Telemetry}

import java.awt.{BasicStroke, Color, Graphics2D}
import java.util


class MapGauge extends ChartPainter {
  val PADDING: Int = 5;
  val MAP_LINE_WIDTH: Int = 3;
  val SHADOW_LINE_WIDTH: Int = 7;
  val POSITION_RADIUS: Int = 5;

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

  override def paint(g: Graphics2D, w: Int, h: Int): Unit = {
    super.paint(g, w, h)

    if (latitudeBoundary == null || longitudeBoundary == null) {
      return
    }
    val scaleX = (w - 2 * PADDING) / GpsFormatter.subtractLongitude(longitudeBoundary.max, longitudeBoundary.min)
    val scaleY = (h - 2 * PADDING) / (latitudeBoundary.max - latitudeBoundary.min)
    val scale = math.min(scaleX, scaleY)

    val yBase = PADDING + ((latitudeBoundary.max - latitudeBoundary.min) * scale).toInt

    drawMap(g, w, h, scale, yBase)
    g.setColor(Color.red)
    g.fillOval(
      PADDING + longitudeToScreen(location.getLongitude, scale) - POSITION_RADIUS,
      (yBase - latitudeToScreen(location.getLatitude, scale)) - POSITION_RADIUS,
      POSITION_RADIUS * 2,
      POSITION_RADIUS * 2,
    )
  }

  private def drawMap(g: Graphics2D, w: Int, h: Int, scale: Double, yBase: Int): Unit = {
    val xPoints: Array[Int] = Array.fill(101){0}
    val yPoints: Array[Int] = Array.fill(101){0}
    for (i <- 0 to 100) {
      val current = telemetry.pointForProgress(i)
      xPoints(i) = PADDING + longitudeToScreen(current.position.getLongitude, scale)
      yPoints(i) = yBase - latitudeToScreen(current.position.getLatitude, scale)
    }
    g.setStroke(new BasicStroke(SHADOW_LINE_WIDTH))
    g.setColor(new Color(0, 0, 0, 128))
    g.drawPolyline(xPoints, yPoints, xPoints.length)
    g.setStroke(new BasicStroke(MAP_LINE_WIDTH))
    g.setColor(Color.yellow)
    g.drawPolyline(xPoints, yPoints, xPoints.length)
  }

  private def longitudeToScreen(lon: Double, scale: Double): Int = {
    return (GpsFormatter.subtractLongitude(lon, longitudeBoundary.min) * scale).toInt;
  }

  private def latitudeToScreen(lat: Double, scale: Double): Int = {
    return ((lat - latitudeBoundary.min) * scale).toInt;
  }
}
