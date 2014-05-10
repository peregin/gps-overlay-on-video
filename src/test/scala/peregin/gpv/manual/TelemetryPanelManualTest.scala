package peregin.gpv.manual

import info.BuildInfo
import scala.swing.{Swing, MainFrame, SimpleSwingApplication}
import peregin.gpv.util.{Io, Logging}
import peregin.gpv.gui.{TelemetryPanel, Goodies, MigPanel}
import java.awt.{Dimension, Color}
import java.io.File
import peregin.gpv.Setup
import peregin.gpv.model.Telemetry
import com.jgoodies.looks.plastic.{Plastic3DLookAndFeel, PlasticTheme, PlasticLookAndFeel}
import javax.swing.UIManager


object TelemetryPanelManualTest extends SimpleSwingApplication with Logging {

  initLookAndFeel

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

  def openGpsFile(file: File) {
    val setup = Setup.empty
    setup.gpsPath = Some(file.getAbsolutePath)
    log.info(s"opening $file")
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(file)
      Swing.onEDT(panel.refresh(setup, telemetry))
    }
  }

  // for testing
  Goodies.showBusy(frame) {
    Thread.sleep(1000)
    val is = Io.getResource("gps/sihlwald.gpx")
    //val telemetry = Telemetry.load(is)
    //Swing.onEDT(panel.refresh(Setup.empty, telemetry))
  }

  // TODO: move this into Goodies and the occurrences as well
  // on Mac start with VM parameter -Xdock:name="GSPonVideo"
  def initLookAndFeel() {
    import PlasticLookAndFeel._
    import collection.JavaConverters._
    sys.props += "apple.laf.useScreenMenuBar" -> "true"
    sys.props += "com.apple.mrj.application.apple.menu.about.name" -> "GPSonVideo"
    val theme = getInstalledThemes.asScala.map(_.asInstanceOf[PlasticTheme]).find(_.getName == "Dark Star")
    theme.foreach(setPlasticTheme)
    UIManager.setLookAndFeel(new Plastic3DLookAndFeel())
  }
}
