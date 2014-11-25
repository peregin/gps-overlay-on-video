package peregin.gpv.manual

import peregin.gpv.model.Telemetry
import peregin.gpv.util.Logging

import scala.xml.XML


object OutliersManualTest extends App with Logging {

  val threshold = 50

  log.info("loading...")
  val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/sihlwald.gpx")))

  telemetry.track.zipWithIndex.foreach{case (tp, ix) =>
    val tab = if (tp.grade.abs > threshold) "\t" else ""
    log.info(s"$tab outlier[$ix] = $tp")
  }

  log.info(s"\n boundaries: ${telemetry.gradeBoundary}")
  val outliers = telemetry.track.count(_.grade > threshold)
  log.info(s"found $outliers outliers out of ${telemetry.track.size}")
}
