package peregin.gpv.manual

import peregin.gpv.{Setup, ConverterDialog}
import peregin.gpv.gui.Goodies


object ConverterDialogManualTest extends App {

  Goodies.initLookAndFeel()

  val file = "/Users/levi/water.json"
  val dialog = new ConverterDialog(Setup.loadFile(file))
  Goodies.center(dialog)
  dialog.open()
  sys.exit()
}
