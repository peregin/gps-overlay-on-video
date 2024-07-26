package peregin.gpv.gui.gauge

import org.jdesktop.swingx.mapviewer.GeoPosition
import peregin.gpv.format.GpsFormatter
import peregin.gpv.model.{InputValue, Sonda}

import java.awt._
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}


class LonLatGauge extends GaugePainter {
  val PADDING: Int = 5;

  var location: GeoPosition = _;
  override def defaultInput: InputValue = null
  override def sample(sonda: Sonda): Unit = {
    location = sonda.location
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    // draw current location
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 3 / 16).toFloat))
    val lonText = GpsFormatter.formatLon(location.getLongitude)
    val latText = GpsFormatter.formatLat(location.getLatitude)

    val bounds = g.getFontMetrics().getStringBounds("W999°99'99.9999\" ", g)

//    backgroundSemiTransparent(g, 0, h / 4 - bounds.getHeight, bounds.getWidth + 2 * PADDING, h * 2 / 4 +1 - (h / 4 - bounds.getHeight) + PADDING)
//    g.setColor(Color.yellow)
//    g.drawString(lonText, 4, h / 4)
//    g.drawString(latText, 4, h * 2 /4 + 1)
    textWidthShadow(g, lonText, 4, h / 4)
    textWidthShadow(g, latText, 4, h * 2 / 4 + 1)
  }
}
