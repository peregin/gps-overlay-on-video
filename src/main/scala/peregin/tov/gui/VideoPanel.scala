package peregin.tov.gui

import java.io.File
import peregin.tov.model.Setup
import javax.swing.filechooser.FileNameExtensionFilter


class VideoPanel(setup: Setup) extends MigPanel("ins 2", "", "[fill]") {

  val chooser = new FileChooserPanel("Load video file:", openVideoData, new FileNameExtensionFilter("Video files (mp4)", "mp4"))
  add(chooser, "pushx, growx, wrap")

  def openVideoData(file: File) {
    setup.videoPath = Some(file.getAbsolutePath)
  }

  def refreshFromSetup() {
    chooser.fileInput.text = setup.videoPath.mkString
  }
}
