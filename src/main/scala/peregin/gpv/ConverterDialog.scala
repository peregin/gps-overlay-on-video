package peregin.gpv

import java.awt.Dimension
import java.io.File

import peregin.gpv.gui.{Goodies, FileChooserPanel, MigPanel}
import peregin.gpv.util.Logging

import scala.swing.{Dialog, Window}


class ConverterDialog(parent: Window = null) extends Dialog(parent) with Logging {

  title = "Converter"
  modal = true
  preferredSize = new Dimension(600, 300)

  Goodies.mapEscapeTo(this, handleCancel)

  contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
    add(new FileChooserPanel("Output", save, null), "")
  }

  def save(file: File) {
    log.info(s"save to ${file.getAbsolutePath}")
  }

  private def handleCancel() {
    log.info("cancel")
    close()
  }
}
