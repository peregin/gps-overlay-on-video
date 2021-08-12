package peregin.gpv.gui.map

import java.awt.{BasicStroke, RenderingHints, Color}
import java.awt.event.{MouseEvent, MouseAdapter}

import org.jdesktop.swingx.{JXMapViewer, JXMapKit}
import org.jdesktop.swingx.mapviewer.GeoPosition
import org.jdesktop.swingx.painter.Painter
import peregin.gpv.model.Telemetry

import scala.swing._
import scala.swing.event.MouseClicked


// map widget
class MapPanel extends JXMapKit with Publisher with KnobPainter {

  private var telemetry = Telemetry.empty()
  private var poi: Option[GeoPosition] = None
  private var progress: Option[GeoPosition] = None

  setDefaultProvider(JXMapKit.DefaultProviders.Custom)
  //setTileFactory(new NasaTileFactory)
  setTileFactory(new MicrosoftTileFactory)
  //setTileFactory(new MapQuestTileFactory)
  setDataProviderCreditShown(true)
  setMiniMapVisible(false)
  setAddressLocation(telemetry.centerGeoPosition)
  setZoom(6)

  getMainMap.addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      publish(new MouseClicked(Component.wrap(MapPanel.this), e.getPoint, e.getModifiers, e.getClickCount, e.isPopupTrigger)(e))
    }
  })

  val routePainter = new Painter[JXMapViewer] {
    override def paint(g2: Graphics2D, `object`: JXMapViewer, width: Int, height: Int) = {
      val g = g2.create().asInstanceOf[Graphics2D]
      // convert from viewport to world bitmap
      val rect = getMainMap.getViewportBounds
      g.translate(-rect.x, -rect.y)

      // do the drawing
      g.setColor(Color.RED)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setStroke(new BasicStroke(2))

      var lastX = -1
      var lastY = -1
      val region = telemetry.track.map(_.position)
      region.foreach{ gp =>
        // convert geo to world bitmap pixel
        val pt = getMainMap.getTileFactory.geoToPixel(gp, getMainMap.getZoom)
        if (lastX != -1 && lastY != -1) {
          g.drawLine(lastX, lastY, pt.getX.toInt, pt.getY.toInt)
        }
        lastX = pt.getX.toInt
        lastY = pt.getY.toInt
      }

      poi.foreach(gp => paintKnob(g, getMainMap.getTileFactory.geoToPixel(gp, getMainMap.getZoom), Color.blue))
      progress.foreach(gp => paintKnob(g, getMainMap.getTileFactory.geoToPixel(gp, getMainMap.getZoom), Color.orange))

      g.dispose()
    }
  }
  getMainMap.setOverlayPainter(routePainter)

  def refresh(telemetry: Telemetry): Unit = {
    this.telemetry = telemetry
  }

  def refreshPoi(sonda: Option[GeoPosition]): Unit = {
    poi = sonda
    repaint()
  }

  def refreshProgress(sonda: Option[GeoPosition]): Unit = {
    progress = sonda
    repaint()
  }
}
