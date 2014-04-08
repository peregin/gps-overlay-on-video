package peregin.tov.model

import java.io.File
import scala.xml.XML
import generated.GpxType
import peregin.tov.util.{Logging, Timed}
import org.jdesktop.swingx.mapviewer.GeoPosition


object Telemetry extends Timed with Logging {
  def load(file: File): Telemetry = timed("load telemetry file") {
    val node = XML.loadFile(file)
    val binding = scalaxb.fromXML[GpxType](node)
    val points = binding.trk.head.trkseg.head.trkpt.map(wyp => TrackPoint(new GeoPosition(wyp.lat.toDouble, wyp.lon.toDouble)))
    log.info(s"found ${points.size} track points")
    new Telemetry(points)
  }

  def empty = new Telemetry(Seq.empty)
}

case class Telemetry(track: Seq[TrackPoint])
