package peregin.gpv.gui

import scala.swing._
import peregin.gpv.util.{Timed, Logging}
import org.jdesktop.swingx.{JXMapViewer, JXMapKit}
import java.io.File
import peregin.gpv.model.{Sonda, Telemetry}
import javax.swing.filechooser.FileNameExtensionFilter
import org.jdesktop.swingx.painter.Painter
import java.awt.{Font, BasicStroke, RenderingHints, Color}
import peregin.gpv.Setup
import peregin.gpv.gui.map.MapQuestTileFactory
import scala.swing.Font
import org.joda.time.DateTime
import scala.swing.event.MouseClicked


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging with Timed {

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
  mapKit.setAddressLocation(telemetry.centerGeoPosition)
  mapKit.setZoom(6)
  add(mapKit, "growx, wrap")

  val routePainter = new Painter[JXMapViewer] {
    override def paint(g2: Graphics2D, `object`: JXMapViewer, width: Int, height: Int) = {
      val g = g2.create().asInstanceOf[Graphics2D]
      // convert from viewport to world bitmap
      val rect = mapKit.getMainMap.getViewportBounds
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
        val pt = mapKit.getMainMap.getTileFactory.geoToPixel(gp, mapKit.getMainMap.getZoom)
        if (lastX != -1 && lastY != -1) {
          g.drawLine(lastX, lastY, pt.getX.toInt, pt.getY.toInt)
        }
        lastX = pt.getX.toInt
        lastY = pt.getY.toInt
      }
      g.dispose()
    }
  }
  mapKit.getMainMap.setOverlayPainter(routePainter)


  // altitude widget
  class AltitudePanel extends Panel {

    private var poi: Option[Sonda] = None

    val elevFont = new Font("Arial", Font.BOLD, 10)
    lazy val elevFm = peer.getGraphics.getFontMetrics(elevFont)
    lazy val metersWidth = elevFm.stringWidth("3000 m")
    lazy val metersHalfHeight = elevFm.getAscent / 2
    lazy val timeWidth = elevFm.stringWidth("00:00:00")

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
        g.drawString(timeLast, gridRight - timeWidth, height - 10 + metersHalfHeight)

        // max distance and speed
        g.setColor(Color.red)
        g.drawString(f"${telemetry.speedBoundary.max}%1.1f", 10, 10 + metersHalfHeight + elevFm.getHeight)
        g.drawString("km/h", 10, 10 + metersHalfHeight + 2 * elevFm.getHeight)
        g.drawString(f"${telemetry.totalDistance}%1.1f", 10, 10 + metersHalfHeight + 4 * elevFm.getHeight)
        g.drawString("km", 10, 10 + metersHalfHeight + 5 * elevFm.getHeight)

        // elevation
        g.setColor(Color.lightGray)
        for (i <- 0 until pxWidth) {
          val f = i.toDouble * 100 / pxWidth // use double value for the percentage
          telemetry.timeForProgress(f).map(telemetry.sonda).foreach{sonda =>
            val v = sonda.elevation.current - telemetry.elevationBoundary.min
            val x = gridLeft + i
            val y = v * pxHeight / mHeight
            //log.info(s"x=$i f=$f elev=${sonda.elevation.current} y=$y")
            g.drawLine(x, gridBottom, x, gridBottom - y.toInt)
          }
        }
      }

      // grid
      g.setColor(Color.gray)
      for (y <- 10 until height - 10 by math.max(1, pxHeight / 6)) {
        g.drawLine(gridLeft, y, gridRight, y)
      }
      for (x <- gridLeft until gridRight by math.max(1, pxWidth / 8)) {
        g.drawLine(x, 10, x, gridBottom)
      }

      // POI marker and data
      poi.foreach{sonda =>
        val p = telemetry.progressForTime(sonda.time)
        val x = (gridLeft + p * pxWidth / 100).toInt
        g.setColor(Color.blue)
        g.drawLine(x, 10, x, gridBottom)
        g.fillOval(x - 5, 5, 10, 10)
        //g.fillOval(x - 5, gridBottom - 5, 10, 10)

        // draw data; time, speed, distance
        g.setColor(Color.blue)
        g.drawString(sonda.time.toString("HH:mm:ss"), gridLeft + (timeWidth * 1.5).toInt, height - 10 + metersHalfHeight)
        g.drawString(f"${sonda.elevation.current}%1.0fm", gridLeft + (timeWidth * 2.9).toInt, height - 10 + metersHalfHeight)
        g.drawString(f"${sonda.distance.current}%1.1fkm", gridLeft + (timeWidth * 3.9).toInt, height - 10 + metersHalfHeight)
        g.drawString(f"${sonda.speed.current}%1.1fkm/h", gridLeft + (timeWidth * 4.9).toInt, height - 10 + metersHalfHeight)
      }
    }

    def timeForPoint(pt: Point): Option[DateTime] = {
      val x = pt.x
      val width = peer.getWidth
      // constants below are defined in the paint method as well
      val gridLeft = 10 + metersWidth
      val gridRight = width - 10
      val progressInPerc = (x - gridLeft).toDouble * 100 / (gridRight - gridLeft)
      //log.info(s"progress $progressInPerc")
      telemetry.timeForProgress(progressInPerc)
    }
    
    def refreshPoi(sonda: Option[Sonda]) {
      poi = sonda
      repaint()
    }
  }
  val altitude = new AltitudePanel
  add(altitude, "pushy, grow, gaptop 10, wrap")

  listenTo(altitude.mouse.clicks)
  reactions += {
    case MouseClicked(`altitude`, pt, _, 1, false) => timed(s"sonda for x=${pt.x}") {
      val sonda = altitude.timeForPoint(pt).map(telemetry.sonda)
      log.info(s"track index = ${sonda.map(_.getTrackIndex).getOrElse(0)}")
      altitude.refreshPoi(sonda)
    }
  }

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.gpsPath.getOrElse("")
    this.telemetry = telemetry

    mapKit.setAddressLocation(telemetry.centerGeoPosition)
    mapKit.repaint()

    altitude.refreshPoi(None)
  }
}
