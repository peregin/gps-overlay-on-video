package peregin.gpv

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.swing._

import info.BuildInfo
import org.jdesktop.swingx._
import peregin.gpv.gui._
import peregin.gpv.gui.gauge.DashboardPainter
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Logging, Timed}
import peregin.gpv.video._

import scala.swing._
import scala.swing.event.{SelectionChanged, ValueChanged}


object GpsOverlayApp extends SimpleSwingApplication with DashboardPainter with VideoPlayer.Listener with Logging with Timed {

  log.info("initializing...")

  Goodies.initLookAndFeel()

  var setup = Setup.empty

  val videoPanel = new VideoPanel(openVideoData, this) with SeekableVideoPlayerFactory
  val telemetryPanel = new TelemetryPanel(openGpsData)
  val statusLabel = new JXLabel("Ready")
  val transparencySlider = new PercentageSlider
  transparencySlider.orientation = Orientation.Vertical
  transparencySlider.percentage = 80

  val unitChooser = new ComboBox(Seq("Metric", "Standard"))

  val frame = new MainFrame {
    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val toolbar = new JToolBar
      toolbar.add(new ImageButton("images/new.png", "New", newProject()))
      toolbar.add(new ImageButton("images/open.png", "Open", openProject()))
      toolbar.add(new ImageButton("images/save.png", "Save", saveProject()))
      toolbar.addSeparator()
      toolbar.add(new ImageButton("images/video.png", "Convert", convertProject()))
      add(toolbar, "span 2, wrap")

      var unitPanel = new MigPanel("ins 0 5 0 5", "", "") {
        add(new Label("Units"), "")
        add(unitChooser, "")
      }
      add(unitPanel, "span 2, wrap")

      //add(titled("Video", videoPanel), "pushy, width 60%")
      add(titled("Video", new MigPanel("ins 0, fill", "[fill]", "[fill]") {
        add(new MigPanel("ins 40 0 40 0, fill", "[fill]", "[fill]") {
          add(new JXLabel("Transparency") {
            setTextRotation(3*Math.PI/2)
            setVerticalAlignment(SwingConstants.CENTER)
            setHorizontalAlignment(SwingConstants.CENTER)
            setMaximumSize(new Dimension(20, 100))
          }, "")
          add(transparencySlider, "")
        }, "align left")
        add(videoPanel, "grow, push")
      }), "pushy, width 60%")
      add(titled("Telemetry Data", telemetryPanel), "pushy, width 40%, wrap")

      val gaugePanel = new GaugePanel
      add(titled("Gauges", new ScrollPane(gaugePanel)), "height 30%")
      val templatePanel = new TemplatePanel
      add(titled("Dashboard templates", templatePanel), "height 30%, wrap")

      val statusPanel = new JXStatusBar
      statusPanel.add(statusLabel)
      add(statusPanel, "pushx, growx")
      val link = new JXHyperlink()
      link.setURI(new URI("www.velocorner.com"))
      add(link, "split, w 150!, align right")
    }
  }

  val spinnerWrap = Component.wrap(telemetryPanel.spinner)
  listenTo(transparencySlider, telemetryPanel.spinner, unitChooser.selection)
  reactions += {
    case ValueChanged(`transparencySlider`) => videoPanel.fireLastVideoEventIfNotPlaying() // will trigger the dashboard repaint
    case ValueChanged(`spinnerWrap`) => videoPanel.fireLastVideoEventIfNotPlaying() // will trigger the dashboard repaint
    case SelectionChanged(`unitChooser`) =>
      val item = unitChooser.selection.item
      log.info(s"switching units to $item")
      setup.units = item
  }

  frame.title = s"GPS data overlay onto video - built ${BuildInfo.buildTime}"
  frame.iconImage = Io.loadImage("images/video.png")
  frame.size = new Dimension(1500, 1000)
  Goodies.center(frame)
  //frame.maximize()

  def top = frame

  def message(s: String): Unit = statusLabel.setText(s)

  def titled(title: String, c: Component): Component = {
    val panel = new JXTitledPanel(title, c.peer)
    Component.wrap(panel)
  }

  def newProject(): Unit = {
    log.info("new project")
    setup = Setup.empty
    val tm = Telemetry.empty
    videoPanel.refresh(setup)
    telemetryPanel.refresh(setup, tm)
    transparencySlider.percentage = setup.transparency
    unitChooser.selection.index = if (setup.units == "Standard") 1 else 0
    message("New project has been created")
  }

  def openProject(): Unit = timed("open project") {
    val chooser = new FileChooser()
    chooser.fileFilter = ExtensionFilters.project
    chooser.title = "Open project:"
    if (chooser.showOpenDialog(GpsOverlayApp.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      val path = file.getAbsolutePath
      debug(s"opening $path")
      Goodies.showBusy(frame) {
        Swing.onEDT {
          Goodies.showPopupOnFailure(frame) {
            message("Loading...")
            setup = Setup.loadFile(path)
            debug(s"setup $setup")
            message("Analyzing telemetry...")
            val telemetry = setup.gpsPath.map(p => Telemetry.load(new File(p)))
            val tm = telemetry.getOrElse(Telemetry.empty)
            message("Updating...")
            videoPanel.refresh(setup)
            telemetryPanel.refresh(setup, tm)
            transparencySlider.percentage = setup.transparency
            message(s"Project $path has been loaded")
          }
        }
      }
    }
  }

  def saveProject(): Unit = {
    val chooser = new FileChooser()
    chooser.fileFilter = ExtensionFilters.project
    chooser.title = "Save project:"
    if (chooser.showSaveDialog(GpsOverlayApp.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      if (!file.exists() ||
          (file.exists() && Dialog.showConfirmation(frame.contents(0), "Do you want to overwrite the file?", "File already exists", Dialog.Options.YesNo) ==  Dialog.Result.Yes)) {
        saveProject(file)
        message(s"Project file has been saved to ${file.getAbsolutePath}")
      }
    }
  }

  private def saveProject(file: File): Unit = {
    log.debug(s"saving ${file.getAbsolutePath}")
    setup.shift = telemetryPanel.getShift
    setup.transparency = transparencySlider.percentage
    setup.saveFile(file.getAbsolutePath)
  }

  def convertProject(): Unit = {
    log.debug("convert project")
    val dialog = new ConverterDialog(setup, telemetryPanel.telemetry, frame)
    Goodies.center(dialog)
    dialog.open()
  }

  def openVideoData(file: File): Unit = {
    setup.videoPath = Some(file.getAbsolutePath)
    videoPanel.refresh(setup)
  }

  def openGpsData(file: File): Unit = {
    setup.gpsPath = Some(file.getAbsolutePath)
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(file)
      Swing.onEDT(telemetryPanel.refresh(setup, telemetry))
    }
  }

  override def seekEvent(percentage: Double): Unit = {}

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage): Unit = {
    paintGauges(telemetryPanel.telemetry, tsInMillis, image, telemetryPanel.getShift, transparencySlider.percentage, unitChooser.selection.item)
    Swing.onEDT(telemetryPanel.updateVideoProgress(tsInMillis))
  }

  override def videoStarted(): Unit = {}

  override def videoStopped(): Unit = {}
}
