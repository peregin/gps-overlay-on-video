package peregin.tov.gui

import scala.swing._
import peregin.tov.util.Logging
import org.jdesktop.swingx.{JXMapViewer, JXMapKit}
import org.jdesktop.swingx.mapviewer.GeoPosition
import java.io.File
import peregin.tov.model.Telemetry
import javax.swing.filechooser.FileNameExtensionFilter
import org.jdesktop.swingx.painter.Painter
import java.awt.{BasicStroke, RenderingHints, Color}
import peregin.tov.Setup


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging {

  var telemetry = Telemetry.empty

  val chooser = new FileChooserPanel("Load GPS data file:", openGpsData, new FileNameExtensionFilter("GPS files (gpx)", "gpx"))
  add(chooser, "pushx, growx, wrap")

  val mapKit = new JXMapKit
  mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps)
  mapKit.setAddressLocation(new GeoPosition(47.366074, 8.541264)) // Buerkliplatz
  add(Component.wrap(mapKit), "growx, wrap")

  val routePainter = new Painter[JXMapViewer] {
    override def paint(g2: Graphics2D, `object`: JXMapViewer, width: Int, height: Int) = {
      val g = g2.create().asInstanceOf[Graphics2D]
      // convert from viewport to world bitmap
      val rect = mapKit.getMainMap().getViewportBounds()
      g.translate(-rect.x, -rect.y)

      // do the drawing
      g.setColor(Color.RED)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setStroke(new BasicStroke(2))

      var lastX = -1
      var lastY = -1
      val region = telemetry.track.map(_.position)
      region.foreach{gp =>
        // convert geo to world bitmap pixel
        val pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom())
        if (lastX != -1 && lastY != -1) {
          g.drawLine(lastX, lastY, pt.getX().toInt, pt.getY().toInt)
        }
        lastX = pt.getX().toInt
        lastY = pt.getY().toInt
      }
      g.dispose()
    }
  }
  mapKit.getMainMap().setOverlayPainter(routePainter)

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.gpsPath.getOrElse("")
    this.telemetry = telemetry
    mapKit.repaint()
  }
}
