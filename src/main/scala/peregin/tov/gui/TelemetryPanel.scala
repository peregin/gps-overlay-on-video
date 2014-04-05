package peregin.tov.gui

import scala.swing.{Label, FileChooser, TextArea, Button}
import scala.swing.event.ButtonClicked
import peregin.tov.App
import peregin.tov.util.Logging


class TelemetryPanel extends MigPanel("", "", "[fill]") with Logging {

  add(new Label("GPS data file:"), "span 2, wrap")
  val browseButton = new Button("Browse")
  add(browseButton, "")
  val fileInput = new TextArea("")
  add(fileInput, "pushx, growx, wrap")

  listenTo(browseButton)
  reactions += {
    case ButtonClicked(`browseButton`) => openData
  }

  def openData = {
    val chooser = new FileChooser()
    if (chooser.showOpenDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"opening ${file.getAbsolutePath}")
      fileInput.text = file.getAbsolutePath
    }
  }
}
