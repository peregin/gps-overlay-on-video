package peregin.gpv.manual

import info.BuildInfo
import scala.swing.{Swing, MainFrame, SimpleSwingApplication}
import peregin.gpv.util.{Io, Logging}
import peregin.gpv.gui.{TelemetryPanel, Goodies, MigPanel}
import java.awt.{Dimension, Color}
import java.io.File
import peregin.gpv.Setup
import peregin.gpv.model.Telemetry


object TelemetryPanelManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  val panel = new TelemetryPanel(openGpsFile)
  val frame = new MainFrame {
    title = s"Telemetry Test - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray
      add(panel, "grow")
    }
  }
  override def top = frame

  frame.size = new Dimension(800, 600)
  Goodies.center(frame)

  def openGpsFile(file: File): Unit = {
    val setup = Setup.empty
    setup.gpsPath = Some(file.getAbsolutePath)
    log.info(s"opening $file")
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(file)
      Swing.onEDT(panel.refresh(setup, telemetry))
    }
  }

  // load test data
  Swing.onEDT {
    Goodies.showBusy(frame) {
      val resource = "gps/sihlwald.gpx"
      val telemetry = Telemetry.load(Io.getResource(resource))
      Swing.onEDT {
        panel.refresh(Setup.empty, telemetry)
        panel.fileChooser.fileInput.text = resource
      }
    }
  }
}
