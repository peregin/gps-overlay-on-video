package peregin.gpv

import java.io.File

import peregin.gpv.gui.{FileChooserPanel, MigPanel}
import peregin.gpv.util.Logging

import scala.swing.{Dialog, Window}


class ConverterDialog(parent: Window = null) extends Dialog(parent) with Logging {

  title = "Converter"
  modal = true

  contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
    add(new FileChooserPanel("Output", save, null), "")
  }

  def save(file: File): Unit = {
    log.info(s"save to ${file.getAbsolutePath}")
  }
}
