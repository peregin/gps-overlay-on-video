package peregin.gpv.manual

import info.BuildInfo
import scala.swing._
import java.awt.{Color, Dimension}
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.gui.gauge.SpeedGauge
import scala.swing.event.ButtonClicked
import peregin.gpv.util.Logging


object GaugeManualTest extends SimpleSwingApplication with Logging {

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray

      add(new SpeedGauge, "")
      add(new SpeedGauge, "wrap")
      add(new SpeedGauge, "span 2, w 150px, h 150px, wrap")
      val testButton = new Button("Test")
      add(testButton, "wrap")

      listenTo(testButton)
      reactions += {
        case ButtonClicked(`testButton`) => testGauge
      }
    }
  }
  frame.size = new Dimension(1024, 768)
  Goodies.center(frame)

  override def top = frame

  def testGauge {
    log.info("test")
  }
}
