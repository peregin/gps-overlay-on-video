package peregin.gpv.manual

import info.BuildInfo
import scala.swing._
import java.awt.Color
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.gui.gauge.SpeedGauge
import peregin.gpv.util.Logging
import javax.swing.{JSlider, JSpinner}


object GaugeManualTest extends SimpleSwingApplication with Logging {

  val minSpinner = new JSpinner
  minSpinner.setValue(0)
  val maxSpinner = new JSpinner
  maxSpinner.setValue(75)
  val slider = new JSlider(0, 75, 32)
  val status = new Label(s"Current Value ${slider.getValue}")

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray

      add(new SpeedGauge, "w 50px, h 50px")
      add(status, "wrap")
      add(new SpeedGauge, "w 100px, h 100px, wrap")
      add(new SpeedGauge, "w 200px, h 200px")
      add(new SpeedGauge, "w 300px, h 300px, wrap")
      val controls = new MigPanel("", "[fill]", "[fill]") {
        add(new Label("Min"), "alignx left")
        add(minSpinner, "alignx left, wmin 50")
        add(slider, "pushx, growx")
        add(maxSpinner, "alignx left, wmin 50")
        add(new Label("Max"), "alignx right")
      }
      add(controls, "span 2, wrap")
    }
  }
  Goodies.center(frame)

  override def top = frame
}
