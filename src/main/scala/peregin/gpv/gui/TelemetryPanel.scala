package peregin.gpv.gui

import java.io.File

import peregin.gpv.Setup
import peregin.gpv.gui.map.{AltitudePanel, MapPanel}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Logging, Timed}

import scala.swing._
import scala.swing.event.MouseClicked


class TelemetryPanel(openGpsData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging with Timed {

  var telemetry = Telemetry.empty

  // file chooser widget
  val chooser = new FileChooserPanel("Load GPS data file:", openGpsData, ExtensionFilters.gps)
  add(chooser, "pushx, growx, wrap")


  val mapKit = new MapPanel
  val mapKitWrapper = Component.wrap(mapKit)
  add(mapKit, "growx, wrap")

  val altitude = new AltitudePanel
  add(altitude, "pushy, grow, gaptop 10, wrap")

  val direction = new ComboBox(Seq("Forward", "Backward"))
  val spinner = new DurationSpinner
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

    mapKit.refresh(telemetry)
    mapKit.setAddressLocation(telemetry.centerGeoPosition)
    mapKit.refreshPoi(None)
    mapKit.refreshProgress(None)

    altitude.refresh(telemetry)
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
