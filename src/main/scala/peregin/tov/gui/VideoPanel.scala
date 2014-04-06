package peregin.tov.gui

import java.io.File
import peregin.tov.model.Setup


class VideoPanel(setup: Setup) extends MigPanel("ins 2", "", "[fill]") {

  val chooser = new FileChooserPanel("Load video file:", openVideoData)
  add(chooser, "pushx, growx, wrap")

  def openVideoData(file: File) {
    setup.videoPath = Some(file.getAbsolutePath)
  }

  def refreshFromSetup() {
    chooser.fileInput.text = setup.videoPath.mkString
  }
}
