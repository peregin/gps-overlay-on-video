package peregin.gpv

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import info.BuildInfo
import org.jdesktop.swingx._
import peregin.gpv.gui._
import peregin.gpv.gui.gauge.DashboardPainter
import peregin.gpv.gui.video._
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Logging, Timed}

import scala.swing._


object App extends SimpleSwingApplication with DashboardPainter with VideoPlayer.Listener with Logging with Timed {

  log.info("initializing...")

  Goodies.initLookAndFeel()

  var setup = Setup.empty

  val videoPanel = new VideoPanel(openVideoData, this) with SeekableVideoPlayerFactory
  val telemetryPanel = new TelemetryPanel(openGpsData)
  val statusLabel = new JXLabel("Ready")

  val frame = new MainFrame {
    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val toolbar = new JToolBar
      toolbar.add(new ImageButton("images/new.png", "New", newProject()))
      toolbar.add(new ImageButton("images/open.png", "Open", openProject()))
      toolbar.add(new ImageButton("images/save.png", "Save", saveProject()))
      toolbar.addSeparator()
      toolbar.add(new ImageButton("images/video.png", "Export", exportProject()))
      add(toolbar, "span 2, wrap")

      add(titled("Video", videoPanel), "pushy, width 60%")
      add(titled("Telemetry Data", telemetryPanel), "pushy, width 40%, wrap")

      val gaugePanel = new GaugePanel
      add(titled("Gauges", new ScrollPane(gaugePanel)), "height 30%")
      val templatePanel = new TemplatePanel
      add(titled("Dashboard templates", templatePanel), "height 30%, wrap")

      val statusPanel = new JXStatusBar
      statusPanel.add(statusLabel)
      add(statusPanel, "pushx, growx")
      val link = new JXHyperlink()
      link.setURI(new URI("www.peregin.com"))
      add(link, "split, w 150!, align right")
    }
  }

  frame.title = s"GPS data overlay onto video - built ${BuildInfo.buildTime}"
  frame.iconImage = Io.loadImage("images/video.png")
  frame.size = new Dimension(1024, 768)
  Goodies.center(frame)
  frame.maximize()

  def top = frame

  def titled(title: String, c: Component): Component = {
    val panel = new JXTitledPanel(title, c.peer)
    Component.wrap(panel)
  }
  
  def newProject() {
    log.info("new project")
    setup = Setup.empty
    val tm = Telemetry.empty
    videoPanel.refresh(setup)
    telemetryPanel.refresh(setup, tm)
  }

  def openProject(): Unit = timed("open project") {
    val chooser = new FileChooser()
    chooser.fileFilter = new FileNameExtensionFilter("project file (json)", "json")
    chooser.title = "Open project:"
    if (chooser.showOpenDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      debug(s"opening ${file.getAbsolutePath}")
      Goodies.showBusy(frame) {
        try {
          setup = Setup.loadFile(file.getAbsolutePath)
          debug(s"setup $setup")
          val telemetry = setup.gpsPath.map(p => Telemetry.load(new File(p)))
          Swing.onEDT {
            val tm = telemetry.getOrElse(Telemetry.empty)
            videoPanel.refresh(setup)
            telemetryPanel.refresh(setup, tm)
          }
        } catch { case any: Throwable =>
          error(s"failed to open $file", any)
          statusLabel.setText(any.getMessage)
        }
      }
    }
  }

  def saveProject() {
    val chooser = new FileChooser()
    chooser.fileFilter = new FileNameExtensionFilter("project file (json)", "json")
    chooser.title = "Save project:"
    if (chooser.showSaveDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"saving ${file.getAbsolutePath}")
      setup.shift = telemetryPanel.getShift
      setup.saveFile(file.getAbsolutePath)
    }
  }

  def exportProject() {
    log.info("export project")
  }

  def openVideoData(file: File) {
    setup.videoPath = Some(file.getAbsolutePath)
    videoPanel.refresh(setup)
  }

  def openGpsData(file: File) {
    setup.gpsPath = Some(file.getAbsolutePath)
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(file)
      Swing.onEDT(telemetryPanel.refresh(setup, telemetry))
    }
  }

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage) {
    paintGauges(telemetryPanel.telemetry, tsInMillis, image, telemetryPanel.getShift)
    Swing.onEDT(telemetryPanel.updateVideoProgress(tsInMillis))
  }

  override def videoStarted() {}

  override def videoStopped() {}
}
