package peregin.gpv.manual

import info.BuildInfo
import scala.swing._
import java.awt.Color
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.gui.gauge.SpeedGauge
import peregin.gpv.util.Logging
import javax.swing.{JSlider, JSpinner}


object GaugeManualTest extends SimpleSwingApplication with Logging {

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray

      add(new SpeedGauge, "w 50px, h 50px")
      add(new Label("Current Value"), "wrap")
      add(new SpeedGauge, "w 100px, h 100px, wrap")
      add(new SpeedGauge, "w 200px, h 200px")
      add(new SpeedGauge, "w 300px, h 300px, wrap")
      val controls = new MigPanel("", "", "") {
        add(new Label("Min"), "alignx left")
        add(new JSpinner, "alignx left")
        add(new JSlider, "pushx, growx")
        add(new JSpinner, "alignx left")
        add(new Label("Max"), "alignx right")
      }
      add(controls, "span 2, wrap")
    }
  }
  Goodies.center(frame)

  override def top = frame
}
