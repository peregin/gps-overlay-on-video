package peregin.gpv.gui.gauge

import org.jdesktop.swingx.mapviewer.GeoPosition
import peregin.gpv.format.GpsFormatter
import peregin.gpv.model.{InputValue, Sonda}

import java.awt._
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}


class LonLatGauge extends GaugePainter {

  var location: GeoPosition = _;
  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    location = sonda.location
  }

  override def paint(g: Graphics2D, w: Int, h: Int): Unit = {
    super.paint(g, w, h)

    // draw current location
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))
    val lonText = GpsFormatter.formatLon(location.getLongitude)
    val latText = GpsFormatter.formatLat(location.getLatitude)
    textOnSemiTransparent(g, lonText, 4, h / 4)
    textOnSemiTransparent(g, latText, 4, h * 2 /4 + 1)
  }
}
