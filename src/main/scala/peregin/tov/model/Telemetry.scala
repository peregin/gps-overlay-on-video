package peregin.tov.model

import java.io.File
import scala.xml.XML
import generated.GpxType
import peregin.tov.util.{Logging, Timed}
import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import javax.xml.datatype.XMLGregorianCalendar
import scala.language.implicitConversions


object Telemetry extends Timed with Logging {

  def load(file: File): Telemetry = timed("load telemetry file") {
    val node = XML.loadFile(file)
    val binding = scalaxb.fromXML[GpxType](node)
    val points = binding.trk.head.trkseg.head.trkpt.map(wyp =>
      TrackPoint(
        new GeoPosition(wyp.lat.toDouble, wyp.lon.toDouble), wyp.ele.map(_.toDouble).getOrElse(0),
        wyp.time
      )
    )
    log.info(s"found ${points.size} track points")
    val data = new Telemetry(points)
    data.analyze()
    log.info(s"elevation boundary ${data.elevationBoundary}")
    data
  }

  implicit def toJoda(xml: Option[XMLGregorianCalendar]): DateTime = {
    xml.map(x => new DateTime(x.toGregorianCalendar.getTime)).getOrElse(sys.error("unable to process data without timestamps"))
  }

  def empty = new Telemetry(Seq.empty)
}

case class Telemetry(track: Seq[TrackPoint]) extends Timed {

  val elevationBoundary = MinMax.extreme
  val latitudeBoundary = MinMax.extreme
  val longitudeBoundary = MinMax.extreme
  var centerPosition = new GeoPosition(47.366074, 8.541264) // Buerkliplatz, Zurich, Switzerland

  def analyze() = timed("analyze GPS data") {
    track.foreach{point =>
      elevationBoundary.sample(point.elevation)
      latitudeBoundary.sample(point.position.getLatitude)
      longitudeBoundary.sample(point.position.getLongitude)
    }
    centerPosition = new GeoPosition(latitudeBoundary.mean, longitudeBoundary.mean)
  }
}
