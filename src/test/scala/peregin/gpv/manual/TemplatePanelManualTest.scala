package peregin.gpv.manual

import java.awt.{Color, Dimension}

import info.BuildInfo
import peregin.gpv.gui.{Goodies, MigPanel, TemplatePanel}
import peregin.gpv.util.Logging

import scala.swing.{MainFrame, SimpleSwingApplication}


object TemplatePanelManualTest extends SimpleSwingApplication with TemplatePanel.Listener with Logging {

  Goodies.initLookAndFeel()

  val panel = new TemplatePanel(this)
  val frame = new MainFrame {
    title = s"Templates Test - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray
      add(panel, "grow")
    }
  }
  override def top = frame

  frame.minimumSize = new Dimension(400, 300)
  Goodies.center(frame)

  override def selected(entry: TemplatePanel.TemplateEntry): Unit = log.info(s"selected $entry")
}
