package peregin.gpv.gui

import java.awt.Image
import java.io.File
import javax.swing.ImageIcon

import peregin.gpv.Setup
import peregin.gpv.gui.map.{AltitudePanel, MapPanel}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Logging, Timed}

import scala.swing._
import scala.swing.event.{ButtonClicked, MouseClicked}


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
  val elevationMode = new ButtonGroup() {
    buttons += new RadioButton("Distance") {
      selected = true
      icon = new ImageIcon(Io.loadImage("images/distance.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH))
      tooltip = "Distance"
    }
    buttons += new RadioButton("Time") {
      selected = false
      icon = new ImageIcon(Io.loadImage("images/time.png").getScaledInstance(16, 16, Image.SCALE_SMOOTH))
      tooltip = "Time"
    }
  }
  val controlPanel = new MigPanel("ins 0 5 0 5", "", "") {
    add(new Label("Shift"), "")
    add(direction, "")
    add(spinner, "align left")
    add(new BoxPanel(Orientation.Horizontal) {contents ++= elevationMode.buttons}, "pushx, align right")
  }
  add(controlPanel, "growx")

  listenTo(altitude.mouse.clicks, mapKit)
  elevationMode.buttons.foreach(ab => listenTo(ab))

  reactions += {
    case MouseClicked(`altitude`, pt, _, 1, false) => timed(s"time/elevation for x=${pt.x}") {
      val sonda = altitude.timeForPoint(pt).map(telemetry.sondaForAbsoluteTime)
      log.info(s"track index = ${sonda.map(_.getTrackIndex).getOrElse(0)}")
      altitude.refreshPoi(sonda)
      mapKit.refreshPoi(sonda.map(_.location))
    }
    case MouseClicked(`mapKitWrapper`, pt, _, 1, false) => timed(s"geo/map for x=${pt.x}, y=${pt.y}") {
      val gp = mapKit.getMainMap.convertPointToGeoPosition(pt)
      log.info(s"geo location $gp")
      val sonda = telemetry.sondaForPosition(gp)
      altitude.refreshPoi(sonda)
      mapKit.refreshPoi(sonda.map(_.location))
    }
    case ButtonClicked(_: RadioButton) =>
      val mode = elevationMode.selected.map(_.text).getOrElse(elevationMode.buttons.head.text) match {
        case "Distance" => altitude.Mode.DistanceBased
        case "Time" => altitude.Mode.TimeBased
      }
      altitude.refresh(mode)
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
    val sonda = telemetry.sondaForRelativeTime(videoTimeInMillis + getShift)
    altitude.refreshProgress(sonda)
    mapKit.refreshProgress(sonda.map(_.location))
  }
}
