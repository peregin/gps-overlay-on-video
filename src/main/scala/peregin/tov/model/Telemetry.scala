package peregin.tov.model

import java.io.File
import scala.xml.XML
import generated.GpxType
import peregin.tov.util.Timed


object Telemetry extends Timed {
  def load(file: File): Telemetry = timed("load telemetry file") {
    val node = XML.loadFile(file)
    val binding = scalaxb.fromXML[GpxType](node)
    val points = binding.trk.head.trkseg.head.trkpt.map(wyp => TrackPoint(wyp.lat.toDouble, wyp.lon.toDouble))
    new Telemetry(points)
  }

  def empty = new Telemetry(Seq.empty)
}

case class Telemetry(track: Seq[TrackPoint])
