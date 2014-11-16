package peregin.gpv.manual

import java.io.File

import peregin.gpv.model.Telemetry
import peregin.gpv.{Setup, ConverterDialog}
import peregin.gpv.gui.Goodies


object ConverterDialogManualTest extends App {

  Goodies.initLookAndFeel()

  val file = "/Users/levi/water.json"
  val setup = Setup.loadFile(file)
  val telemetry = Telemetry.load(new File(setup.gpsPath.getOrElse(sys.error("gps file is not configured"))))
  val dialog = new ConverterDialog(setup, telemetry)
  Goodies.center(dialog)
  dialog.open()
  sys.exit()
}
