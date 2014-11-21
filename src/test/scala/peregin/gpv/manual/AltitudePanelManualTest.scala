package peregin.gpv.manual

import java.awt.{Color, Dimension}

import info.BuildInfo
import peregin.gpv.gui.map.AltitudePanel
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Logging}

import scala.swing.{MainFrame, SimpleSwingApplication, Swing}


object AltitudePanelManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  val panel = new AltitudePanel
  val frame = new MainFrame {
    title = s"Elevation Profile Test - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray
      add(panel, "grow")
    }
  }
  override def top = frame

  // for testing
  val telemetry = Telemetry.load(Io.getResource("gps/sihlwald.gpx"))
  Swing.onEDT(panel.refresh(telemetry))

  frame.size = new Dimension(800, 300)
  Goodies.center(frame)
}
