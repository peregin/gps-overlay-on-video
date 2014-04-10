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
    val points = binding.trk.head.trkseg.head.trkpt.map(wyp =>
      TrackPoint(
        new GeoPosition(wyp.lat.toDouble, wyp.lon.toDouble),
        wyp.ele.map(_.toDouble).getOrElse(0)
      )
    )
    log.info(s"found ${points.size} track points")
    val data = new Telemetry(points)
    data.analyze()
    log.info(s"elevation boundary ${data.elevationBoundary}")
    data
  }

  def empty = new Telemetry(Seq.empty)
}

case class Telemetry(track: Seq[TrackPoint]) {

  val elevationBoundary = MinMax.extreme
  val latitudeBoundary = MinMax.extreme
  val longitudeBoundary = MinMax.extreme
  var centerPosition = new GeoPosition(47.366074, 8.541264) // Buerkliplatz, Zurich

  def analyze() {
    track.foreach{point =>
      elevationBoundary.sample(point.elevation)
      latitudeBoundary.sample(point.position.getLatitude)
      longitudeBoundary.sample(point.position.getLongitude)
    }
    centerPosition = new GeoPosition(latitudeBoundary.middle, longitudeBoundary.middle)
  }
}
