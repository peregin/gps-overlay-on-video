package peregin.gpv.gui

import scala.swing._
import peregin.gpv.util.Logging
import org.jdesktop.swingx.{JXMapViewer, JXMapKit}
import java.io.File
import peregin.gpv.model.Telemetry
import javax.swing.filechooser.FileNameExtensionFilter
import org.jdesktop.swingx.painter.Painter
import java.awt.{Font, BasicStroke, RenderingHints, Color}
import peregin.gpv.Setup
import peregin.gpv.gui.map.MapQuestTileFactory
import scala.swing.Font
import org.joda.time.DateTime
import scala.swing.event.MouseClicked


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging {

  var telemetry = Telemetry.empty

  // file chooser widget
  val chooser = new FileChooserPanel("Load GPS data file:", openGpsData, new FileNameExtensionFilter("GPS files (gpx)", "gpx"))
  add(chooser, "pushx, growx, wrap")


  // map widget
  val mapKit = new JXMapKit
  mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps)
  //mapKit.setTileFactory(new NasaTileFactory)
  mapKit.setTileFactory(new MapQuestTileFactory)
  mapKit.setDataProviderCreditShown(true)
  mapKit.setMiniMapVisible(false)
  mapKit.setAddressLocation(telemetry.centerPosition)
  mapKit.setZoom(6)
  add(mapKit, "growx, wrap")

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


  // altitude widget
  class AltitudePanel extends Panel {

    private var poi: Option[DateTime] = None

    val elevFont = new Font("Arial", Font.BOLD, 10)
    lazy val elevFm = peer.getGraphics.getFontMetrics(elevFont)
    lazy val metersWidth = elevFm.stringWidth("3000 m")
    lazy val metersHalfHeight = elevFm.getAscent / 2

    override def paint(g: Graphics2D) = {
      val width = peer.getWidth
      val height = peer.getHeight

      // background
      g.setColor(Color.white)
      g.fillRect(0, 0, width, height)

      // some predefined values for painting
      g.setFont(elevFont)
      // this is used in the point2Time function as well
      val gridLeft = 10 + metersWidth
      val gridRight = width - 10
      val gridBottom = height - 10 - elevFm.getHeight
      val pxWidth = gridRight - gridLeft
      val pxHeight = height - 20 - elevFm.getHeight

      // coordinates, only if the track is not empty
      if (!telemetry.track.isEmpty) {
        val mHeight = telemetry.elevationBoundary.diff

        // legend
        g.setColor(Color.black)
        g.drawString(s"${telemetry.elevationBoundary.max.toInt} m", 10, 10 + metersHalfHeight)
        g.drawString(s"${telemetry.elevationBoundary.min.toInt} m", 10, height - 10 - elevFm.getHeight + metersHalfHeight)
        val timeFirst = telemetry.minTime.toString("HH:mm:ss")
        val timeLast = telemetry.maxTime.toString("HH:mm:ss")
        g.drawString(timeFirst, gridLeft, height - 10 + metersHalfHeight)
        g.drawString(timeLast, gridRight - elevFm.stringWidth(timeLast), height - 10 + metersHalfHeight)

        // elevation
        g.setColor(Color.lightGray)
        // TODO: use time interpolation here, just painting based on the index is inaccurate
        val trackWidth = telemetry.track.size
        for (i <- 0 until trackWidth) {
          val v = telemetry.track(i).elevation - telemetry.elevationBoundary.min
          val x = gridLeft + (i * pxWidth) / trackWidth
          val y = (v * pxHeight) / mHeight
          g.drawLine(x, gridBottom, x, gridBottom - y.toInt)
        }
      }

      // grid
      g.setColor(Color.gray)
      for (y <- 10 until height - 10 by pxHeight / 6) {
        g.drawLine(gridLeft, y, gridRight, y)
      }
      for (x <- gridLeft until gridRight by pxWidth / 8) {
        g.drawLine(x, 10, x, gridBottom)
      }

      // POI
      poi.foreach{t =>
        val p = telemetry.progressForTime(t)
        val x = (gridLeft + p * pxWidth / 100).toInt
        g.setColor(Color.blue)
        g.drawLine(x, 10, x, gridBottom)
        g.fillOval(x - 5, 5, 10, 10)
        g.fillOval(x - 5, gridBottom - 5, 10, 10)
      }
    }

    def timeForPoint(pt: Point): Option[DateTime] = {
      val x = pt.x.toDouble
      val width = peer.getWidth
      // constants below are defined in the paint method as well
      val gridLeft = 10 + metersWidth
      val gridRight = width - 10
      val progressInPerc = (x - gridLeft) * 100 / (gridRight - gridLeft)
      telemetry.timeForProgress(progressInPerc)
    }
    
    def refreshPoi(t: Option[DateTime]) {
      poi = t
      repaint()
    }
  }
  val altitude = new AltitudePanel
  add(altitude, "pushy, grow, gaptop 10, wrap")

  listenTo(altitude.mouse.clicks)
  reactions += {
    case MouseClicked(`altitude`, pt, _, 1, false) => altitude.refreshPoi(altitude.timeForPoint(pt))
  }

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.gpsPath.getOrElse("")
    this.telemetry = telemetry

    mapKit.setAddressLocation(telemetry.centerPosition)
    mapKit.repaint()

    altitude.refreshPoi(None)
  }
}
