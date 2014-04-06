package peregin.tov.gui

import java.io.File


class VideoPanel extends MigPanel("ins 2", "", "[fill]") {

  val chooser = new FileChooserPanel("Load video file:", openVideoData)
  add(chooser, "pushx, growx, wrap")

  def openVideoData(file: File) {

  }
}
