package peregin.gpv

import java.awt.Dimension
import java.io.File

import peregin.gpv.gui._
import peregin.gpv.util.Logging

import scala.swing.event.ButtonClicked
import scala.swing.{Button, Separator, Dialog, Window}


class ConverterDialog(setup: Setup, parent: Window = null) extends Dialog(parent) with Logging {

  title = "Converter"
  modal = true
  preferredSize = new Dimension(400, 300)

  private val imagePanel = new ImagePanel
  private val okButton = new Button("Ok")
  private val cancelButton = new Button("Cancel")

  private val chooser = new FileChooserPanel("File name of the new video:", save, ExtensionFilters.video, false)
  chooser.fileInput.text = setup.outputPath.getOrElse("")

  contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
    add(imagePanel, "grow, pushy, wrap")
    add(chooser, "wrap")
    add(new Separator(), "growx, wrap")
    add(new MigPanel("ins 5, align center", "[center]", "") {
      add(okButton, "sg 1, w 80, center")
      add(cancelButton, "sg 1, w 80, center")
    }, "dock south")
  }

  Goodies.mapEscapeTo(this, handleCancel)

  listenTo(okButton, cancelButton)
  reactions += {
    case ButtonClicked(`okButton`) => handleOk()
    case ButtonClicked(`cancelButton`) => handleCancel()
  }

  def save(file: File) {
    setup.outputPath = Some(file.getAbsolutePath)
    log.info(s"save to ${file.getAbsolutePath}")
  }

  private def handleOk() {
    log.info("ok")
    close()
  }

  private def handleCancel() {
    log.info("cancel")
    close()
  }
}
