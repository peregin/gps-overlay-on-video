package peregin.gpv

import info.BuildInfo
import scala.swing._
import java.awt.Dimension
import peregin.gpv.gui._
import javax.swing._
import org.jdesktop.swingx._
import peregin.gpv.util.{Io, Timed, Logging}
import peregin.gpv.model.Telemetry
import javax.swing.filechooser.FileNameExtensionFilter
import java.io.File
import java.net.URI


object App extends SimpleSwingApplication with Logging with Timed {

  log.info("initializing...")

  Goodies.initLookAndFeel()

  var setup = Setup.empty

  val videoPanel = new VideoPanel(openVideoData, showVideoProgress)
  val telemetryPanel = new TelemetryPanel(openGpsData)

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
      statusPanel.add(new JXLabel("Ready"))
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
    videoPanel.refresh(setup, tm)
    telemetryPanel.refresh(setup, tm)
  }

  def openProject(): Unit = timed("open project") {
    val chooser = new FileChooser()
    chooser.fileFilter = new FileNameExtensionFilter("project file (json)", "json")
    chooser.title = "Open project:"
    if (chooser.showOpenDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"opening ${file.getAbsolutePath}")
      Goodies.showBusy(frame) {
        setup = Setup.loadFile(file.getAbsolutePath)
        val telemetry = setup.gpsPath.map(p => Telemetry.load(new File(p)))
        Swing.onEDT {
          val tm = telemetry.getOrElse(Telemetry.empty)
          videoPanel.refresh(setup, tm)
          telemetryPanel.refresh(setup, tm)
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
      setup.saveFile(file.getAbsolutePath)
    }
  }

  def exportProject() {
    log.info("export project")
  }

  def openVideoData(file: File) {
    setup.videoPath = Some(file.getAbsolutePath)
    videoPanel.refresh(setup, telemetryPanel.telemetry)
  }

  def openGpsData(file: File) {
    setup.gpsPath = Some(file.getAbsolutePath)
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(file)
      Swing.onEDT(telemetryPanel.refresh(setup, telemetry))
    }
  }

  def showVideoProgress(videoTimeInMillis: Long) {
    Swing.onEDT(telemetryPanel.showVideoProgress(videoTimeInMillis))
  }
}
