package peregin.gpv.manual

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension}
import java.io.File

import info.BuildInfo
import peregin.gpv.Setup
import peregin.gpv.video._
import peregin.gpv.gui.{Goodies, MigPanel, VideoPanel}
import peregin.gpv.util.Logging

import scala.swing.{MainFrame, SimpleSwingApplication}


object VideoPanelManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  val video = new VideoPanel(openVideoFile, new VideoPlayer.Listener() {
    override def seekEvent(percentage: Double) {}
    override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage) {}
    override def videoStarted() {}
    override def videoStopped() {}
  }) with SeekableVideoPlayerFactory

  val frame = new MainFrame {
    title = s"Video Test - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray
      add(video, "grow")
    }
  }
  override def top = frame

  frame.size = new Dimension(800, 600)
  Goodies.center(frame)

  def openVideoFile(file: File) {
    val setup = Setup.empty
    setup.videoPath = Some(file.getAbsolutePath)
    log.info(s"opening $file")
    video.refresh(setup)
  }

  // for convenience in testing, try to load the video file if first argument is configured
  override def main(args: Array[String]) {
    super.main(args)
    args.headOption.map(new File(_)).foreach(openVideoFile)
  }
}
