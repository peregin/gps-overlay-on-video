package peregin.tov.model

import java.io.File
import scala.xml.XML
import generated.GpxType

object Telemetry {
  def load(file: File): Telemetry = {
    val node = XML.loadFile(file)
    val binding = scalaxb.fromXML[GpxType](node)
    val points = binding.trk.head.trkseg.head.trkpt.map(wyp => TrackPoint(wyp.lat.toDouble, wyp.lon.toDouble))
    new Telemetry(points)
  }
}

case class Telemetry(track: Seq[TrackPoint])
