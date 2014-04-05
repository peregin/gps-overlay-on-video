package peregin.tov.gui

import java.io.File


class VideoPanel extends MigPanel("", "", "[fill]") {

  val chooser = new FileChooserPanel("Video file:", openVideoData)
  add(chooser, "pushx, growx, wrap")

  def openVideoData(file: File) {

  }
}
