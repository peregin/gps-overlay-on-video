package peregin.gpv.model

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime

import scala.collection.mutable.ArrayBuffer

object TelemetryTestUtil {

  /**
   * Builds telemetry from lat,lon pairs.
   */
  def buildTelemetry(positions: Double*): Telemetry = {
    val telemetry: Telemetry = Telemetry.loadWith(buildTrack(positions: _*))
    telemetry
  }

  /**
   * Builds TrackPoint sequence from lat,lon pairs.
   */
  def buildTrack(positions: Double*): Seq[TrackPoint] = {
    val tps: ArrayBuffer[TrackPoint] = new ArrayBuffer[TrackPoint];
    for (i <- 0 until positions.length by 2) {
      tps.addOne(TrackPoint(new GeoPosition(positions(i + 0), positions(i + 1)), 0, new DateTime(i / 2 * 1000L), GarminExtension(Some(i / 2), Some(i / 2), Some(i / 2), Some(i / 2))))
    }
    tps.toSeq
  }

}
