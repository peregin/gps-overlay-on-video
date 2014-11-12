package peregin.gpv.manual

import peregin.gpv.ConverterDialog
import peregin.gpv.gui.Goodies


object ConverterDialogManualTest extends App {

  Goodies.initLookAndFeel()

  val dialog = new ConverterDialog()
  Goodies.center(dialog)
  dialog.open()
  sys.exit()
}
