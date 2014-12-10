package peregin.gpv.manual

import java.awt.{Dimension, Color}

import info.BuildInfo
import peregin.gpv.gui.{Goodies, GaugePanel, MigPanel}

import scala.swing.{MainFrame, SimpleSwingApplication}

/**
 * Created by peregin on 10/12/14.
 */
object GaugePanelManualTest extends SimpleSwingApplication {

  Goodies.initLookAndFeel()

  val panel = new GaugePanel
  val frame = new MainFrame {
    title = s"Gauge Selector Test - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray
      add(panel, "grow")
    }
  }
  override def top = frame

  frame.minimumSize = new Dimension(800, 250)
  Goodies.center(frame)
}
