package peregin.tov.gui

import java.io.File
import peregin.tov.util.Logging
import scala.swing.{FileChooser, TextArea, Button, Label}
import scala.swing.event.ButtonClicked
import peregin.tov.App


class FileChooserPanel(info: String, action: File => Unit) extends MigPanel("ins 0", "", "[grow, fill]") with Logging {
  add(new Label(info), "span 2, wrap")
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
      action(file)
    }
  }
}
