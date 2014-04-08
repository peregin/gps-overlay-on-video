package peregin.tov.gui

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import peregin.tov.model.Setup


class VideoPanel(openVideoData: File => Unit) extends MigPanel("ins 2", "", "[fill]") {

  val chooser = new FileChooserPanel("Load video file:", openVideoData, new FileNameExtensionFilter("Video files (mp4)", "mp4"))
  add(chooser, "pushx, growx, wrap")

  def refresh(setup: Setup) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
  }
}
