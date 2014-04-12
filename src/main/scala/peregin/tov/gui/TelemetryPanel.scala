package peregin.tov.gui

import scala.swing._
import peregin.tov.util.Logging
import org.jdesktop.swingx.{JXMapViewer, JXMapKit}
import java.io.File
import peregin.tov.model.Telemetry
import javax.swing.filechooser.FileNameExtensionFilter
import org.jdesktop.swingx.painter.Painter
import java.awt.{BasicStroke, RenderingHints, Color}
import peregin.tov.Setup
import peregin.tov.gui.map.MapQuestTileFactory


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging {

  var telemetry = Telemetry.empty

  val chooser = new FileChooserPanel("Load GPS data file:", openGpsData, new FileNameExtensionFilter("GPS files (gpx)", "gpx"))
  add(chooser, "pushx, growx, wrap")

  val mapKit = new JXMapKit
  mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps)
  //mapKit.setTileFactory(new NasaTileFactory)
  mapKit.setTileFactory(new MapQuestTileFactory)
  mapKit.setDataProviderCreditShown(true)
  mapKit.setMiniMapVisible(false)
  mapKit.setAddressLocation(telemetry.centerPosition)
  mapKit.setZoom(6)
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

  class AltitudePanel extends Panel {
    override def paint(g: Graphics2D) = {
      g.setColor(Color.white)
      val width = peer.getWidth
      val height = peer.getHeight
      g.fillRect(0, 0, width, height)
    }
  }
  val altitude = new AltitudePanel
  add(altitude, "pushy, grow, gaptop 10, wrap")

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.gpsPath.getOrElse("")
    this.telemetry = telemetry
    mapKit.setAddressLocation(telemetry.centerPosition)
    mapKit.repaint()
  }
}

//object MapQuestTileFactoryInfo {
//  private val MAX_ZOOM = 17
//}
//
//import MapQuestTileFactoryInfo._
//class MapQuestTileFactoryInfo extends TileFactoryInfo(
//  MAX_ZOOM - 2, MAX_ZOOM, 256, true, true,
//  //"http://oatile1.mqcdn.com/tiles/1.0.0/sat", //aerial tiles
//  "http://otile1.mqcdn.com/tiles/1.0.0/osm", //Mapquest OSM
//  "x", "y", "z") {
//    override def getTileUrl(x: Int, y: Int, zoom: Int): String = {
//      val z = MAX_ZOOM - zoom
//      this.baseURL + "/" + z + "/" + x + "/" + y + ".png"
//    }
//}
