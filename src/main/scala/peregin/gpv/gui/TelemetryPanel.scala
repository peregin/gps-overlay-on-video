package peregin.gpv.gui

import java.awt.event.{MouseAdapter, MouseEvent}
import java.awt.{BasicStroke, Color, Font, RenderingHints}
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.jdesktop.swingx.painter.Painter
import org.jdesktop.swingx.{JXMapKit, JXMapViewer}
import org.joda.time.DateTime
import peregin.gpv.Setup
import peregin.gpv.gui.map.{KnobPainter, MapQuestTileFactory}
import peregin.gpv.model.{Sonda, Telemetry}
import peregin.gpv.util.{Logging, Timed}

import scala.swing.{Font, _}
import scala.swing.event.MouseClicked


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]")
  with KnobPainter with Logging with Timed {

  var telemetry = Telemetry.empty

  // file chooser widget
  val chooser = new FileChooserPanel("Load GPS data file:", openGpsData, new FileNameExtensionFilter("GPS files (gpx)", "gpx"))
  add(chooser, "pushx, growx, wrap")


  // map widget
  class MapPanel extends JXMapKit with Publisher {

    private var poi: Option[GeoPosition] = None
    private var progress: Option[GeoPosition] = None

    setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps)
    //setTileFactory(new NasaTileFactory)
    setTileFactory(new MapQuestTileFactory)
    setDataProviderCreditShown(true)
    setMiniMapVisible(false)
    setAddressLocation(telemetry.centerGeoPosition)
    setZoom(6)

    getMainMap.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) {
        MapPanel.this.publish(new MouseClicked(MapPanel.this, e.getPoint, e.getModifiers, e.getClickCount, e.isPopupTrigger)(e))
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

    def refreshPoi(sonda: Option[GeoPosition]) {
      poi = sonda
      repaint()
    }

    def refreshProgress(sonda: Option[GeoPosition]) {
      progress = sonda
      repaint()
    }
  }

  // altitude widget
  class AltitudePanel extends Panel {

    private var poi: Option[Sonda] = None
    private var progress: Option[Sonda] = None

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
      if (telemetry.track.nonEmpty) {
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
        paintKnob(g, x, 10, Color.blue)

        // draw data; time, speed, distance
        g.setColor(Color.blue)
        g.drawString(sonda.time.toString("HH:mm:ss"), gridLeft + (timeWidth * 1.5).toInt, height - 10 + metersHalfHeight)
        g.drawString(f"${sonda.elevation.current}%1.0fm", gridLeft + (timeWidth * 2.9).toInt, height - 10 + metersHalfHeight)
        g.drawString(f"${sonda.distance.current}%1.1fkm", gridLeft + (timeWidth * 3.9).toInt, height - 10 + metersHalfHeight)
        g.drawString(f"${sonda.speed.current}%1.1fkm/h", gridLeft + (timeWidth * 4.9).toInt, height - 10 + metersHalfHeight)
      }

      // progress when playing the video
      progress.foreach{sonda =>
        val p = telemetry.progressForTime(sonda.time)
        val x = (gridLeft + p * pxWidth / 100).toInt
        g.setColor(Color.orange)
        g.drawLine(x, 10, x, gridBottom)
        paintKnob(g, x, 10, Color.orange)
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

    def refreshProgress(sonda: Option[Sonda]) {
      progress = sonda
      repaint()
    }
  }

  val mapKit = new MapPanel
  val mapKitWrapper = Component.wrap(mapKit)
  add(mapKit, "growx, wrap")

  val altitude = new AltitudePanel
  add(altitude, "pushy, grow, gaptop 10, wrap")

  val spinner = new DurationSpinner
  val direction = new ComboBox(Seq("Forward", "Backward"))
  val controlPanel = new MigPanel("ins 0", "", "") {
    add(new Label("Shift"), "")
    add(direction, "")
    add(spinner, "align left")
  }
  add(controlPanel, "growx")

  listenTo(altitude.mouse.clicks, mapKit)
  reactions += {
    case MouseClicked(`altitude`, pt, _, 1, false) => timed(s"time/elevation for x=${pt.x}") {
      val sonda = altitude.timeForPoint(pt).map(telemetry.sonda)
      log.info(s"track index = ${sonda.map(_.getTrackIndex).getOrElse(0)}")
      altitude.refreshPoi(sonda)
      mapKit.refreshPoi(sonda.map(_.location))
    }
    case MouseClicked(`mapKitWrapper`, pt, _, 1, false) => timed(s"geo/map for x=${pt.x}, y=${pt.y}") {
      val gp = mapKit.getMainMap.convertPointToGeoPosition(pt)
      log.info(s"geo location $gp")
      val sonda = telemetry.sonda(gp)
      altitude.refreshPoi(sonda)
      mapKit.refreshPoi(sonda.map(_.location))
    }
  }

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.gpsPath.getOrElse("")
    this.telemetry = telemetry

    mapKit.setAddressLocation(telemetry.centerGeoPosition)
    mapKit.refreshPoi(None)
    mapKit.refreshProgress(None)

    altitude.refreshPoi(None)
    altitude.refreshProgress(None)

    spinner.duration = setup.shift.abs
    direction.selection.index = if (setup.shift < 0) 1 else 0
  }

  def getShift = spinner.duration * (if (direction.selection.index == 0) 1 else -1)

  // dispatched by the video controller, invoked from EDT
  def updateVideoProgress(videoTimeInMillis: Long) {
    val sonda = telemetry.sonda(videoTimeInMillis + getShift)
    altitude.refreshProgress(sonda)
    mapKit.refreshProgress(sonda.map(_.location))
  }
}
