package peregin.gpv.manual

import info.BuildInfo
import scala.swing._
import java.awt.Color
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.gui.gauge.SpeedGauge
import scala.swing.event.ButtonClicked
import peregin.gpv.util.Logging


object GaugeManualTest extends SimpleSwingApplication with Logging {

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray

      add(new SpeedGauge, "w 50px, h 50px, wrap")
      add(new SpeedGauge, "w 100px, h 100px, wrap")
      add(new SpeedGauge, "w 200px, h 200px")
      add(new SpeedGauge, "w 300px, h 300px, wrap")
      val testButton = new Button("Test")
      add(testButton, "wrap")

      listenTo(testButton)
      reactions += {
        case ButtonClicked(`testButton`) => testGauge
      }
    }
  }
  Goodies.center(frame)

  override def top = frame

  def testGauge {
    log.info("test")
  }
}
