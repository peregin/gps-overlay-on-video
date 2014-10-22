package peregin.gpv.manual

import info.BuildInfo
import peregin.gpv.gui.video._
import scala.swing.{MainFrame, SimpleSwingApplication}
import peregin.gpv.util.Logging
import peregin.gpv.gui.{VideoPanel, Goodies, MigPanel}
import java.awt.{Dimension, Color}
import java.io.File
import peregin.gpv.Setup
import peregin.gpv.model.Telemetry


object VideoPanelManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  val video = new VideoPanel(openVideoFile, (Long) => {}, 0L) with SimpleVideoPlayerFactory
  val frame = new MainFrame {
    title = s"Video Test - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray
      add(video, "grow")
    }
  }
  override def top = frame

  frame.size = new Dimension(1024, 768)
  Goodies.center(frame)

  def openVideoFile(file: File) {
    val setup = Setup.empty
    setup.videoPath = Some(file.getAbsolutePath)
    log.info(s"opening $file")
    video.refresh(setup, Telemetry.empty)
  }
}
