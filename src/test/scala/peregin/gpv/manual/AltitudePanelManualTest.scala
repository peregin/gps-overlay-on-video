package peregin.gpv.manual

import java.awt.{Color, Dimension}

import info.BuildInfo
import peregin.gpv.gui.map.AltitudePanel
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Timed, Io, Logging}

import scala.swing.event.MouseClicked
import scala.swing.{MainFrame, SimpleSwingApplication, Swing}


object AltitudePanelManualTest extends SimpleSwingApplication with Logging with Timed {

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

  listenTo(panel.mouse.clicks)
  reactions += {
    case MouseClicked(`panel`, pt, _, 1, false) => timed(s"time/elevation for x=${pt.x}") {
      panel.refreshPoi(panel.sondaForPoint(pt))
    }
  }

  frame.size = new Dimension(800, 300)
  Goodies.center(frame)

  // load test data
  Swing.onEDT {
    Goodies.showBusy(frame) {
      val telemetry = Telemetry.load(Io.getResource("gps/sihlwald.gpx"))
      Swing.onEDT{
        panel.refresh(telemetry)
        panel.repaint()
      }
    }
  }
}
