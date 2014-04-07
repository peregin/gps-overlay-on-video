package peregin.tov.gui

import scala.swing._
import peregin.tov.util.Logging
import org.jdesktop.swingx.{JXMapViewer, JXMapKit}
import org.jdesktop.swingx.mapviewer.GeoPosition
import java.io.File
import peregin.tov.model.{Telemetry, Setup}
import javax.swing.filechooser.FileNameExtensionFilter
import org.jdesktop.swingx.painter.Painter
import java.awt.{BasicStroke, RenderingHints, Color}


class TelemetryPanel(setup: Setup) extends MigPanel("ins 2", "", "[fill]") with Logging {

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
      setup.telemetry.foreach{tm =>
        val region = tm.track.map(tp => new GeoPosition(tp.latitude, tp.longitude))
        region.foreach{gp =>
          // convert geo to world bitmap pixel
          val pt = mapKit.getMainMap().getTileFactory().geoToPixel(gp, mapKit.getMainMap().getZoom())
          if (lastX != -1 && lastY != -1) {
            g.drawLine(lastX, lastY, pt.getX().toInt, pt.getY().toInt)
          }
          lastX = pt.getX().toInt
          lastY = pt.getY().toInt
        }
      }
      g.dispose()
    }
  }
  mapKit.getMainMap().setOverlayPainter(routePainter)

  def openGpsData(file: File) {
    setup.telemetryPath = Some(file.getAbsolutePath)
    load(file)
  }

  def refreshFromSetup() {
    chooser.fileInput.text = setup.telemetryPath.mkString
    setup.telemetryPath match {
      case Some(path) => load(new File(path))
      case  _ => mapKit.repaint()
    }
  }

  def load(file: File) {
    setup.telemetry = Some(Telemetry.load(file))
    mapKit.repaint()
  }
}
