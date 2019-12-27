package peregin.gpv.manual

import java.io.File

import peregin.gpv.model.Telemetry
import peregin.gpv.{ConverterDialog, Setup}
import peregin.gpv.gui.Goodies
import peregin.gpv.gui.TemplatePanel.TemplateEntry
import peregin.gpv.gui.dashboard.CyclingDashboard


object ConverterDialogManualTest extends App {

  Goodies.initLookAndFeel()

  val file = "src/test/resources/project/water.json"
  val setup = Setup.loadFile(file)
  val telemetry = Telemetry.load(new File(setup.gpsPath.getOrElse(sys.error("gps file is not configured"))))
  val template = TemplateEntry("Cycling", new CyclingDashboard {})
  val dialog = new ConverterDialog(setup, telemetry, template)
  Goodies.center(dialog)
  dialog.open()
  sys.exit()
}
