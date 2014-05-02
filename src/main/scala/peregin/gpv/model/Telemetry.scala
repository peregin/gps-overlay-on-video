package peregin.gpv.model

import java.io.File
import scala.xml.{Node, XML}
import generated.GpxType
import peregin.gpv.util.{Logging, Timed}
import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import javax.xml.datatype.XMLGregorianCalendar
import scala.language.implicitConversions


object Telemetry extends Timed with Logging {

  def load(file: File): Telemetry = loadWith(XML.loadFile(file))
  
  def loadWith(loadFunc: => Node): Telemetry = timed("load telemetry") {
    val node = loadFunc
    val binding = scalaxb.fromXML[GpxType](node)
    val points = binding.trk.head.trkseg.head.trkpt.map(wyp =>
      TrackPoint(
        new GeoPosition(wyp.lat.toDouble, wyp.lon.toDouble), wyp.ele.map(_.toDouble).getOrElse(0d),
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

case class Telemetry(track: Seq[TrackPoint]) extends Timed with Logging {

  val elevationBoundary = MinMax.extreme
  val latitudeBoundary = MinMax.extreme
  val longitudeBoundary = MinMax.extreme
  val speedBoundary = MinMax.extreme
  val gradeBoundary = MinMax.extreme

  private var centerPosition = TrackPoint.centerPosition

  def analyze() = timed("analyze GPS data") {
    val n = track.size
    for (i <- 0 until n) {
      val point = track(i)
      elevationBoundary.sample(point.elevation)
      latitudeBoundary.sample(point.position.getLatitude)
      longitudeBoundary.sample(point.position.getLongitude)
      if (i < n - 1) {
        val nextPoint = track(i + 1)
        point.analyze(nextPoint)
        speedBoundary.sample(point.speed)
        gradeBoundary.sample(point.grade)
      }
    }
    centerPosition = new GeoPosition(latitudeBoundary.mean, longitudeBoundary.mean)
  }

  def centerGeoPosition = centerPosition

  def minTime = track.head.time
  def maxTime = track.last.time

  def totalDistance = track.last.distance

  /**
   * retrieves the interpolated time for the given progress
   * @param progressInPerc is defined between 0 and 100
   */
  def timeForProgress(progressInPerc: Double): Option[DateTime] = {
    if (progressInPerc <= 0d) track.headOption.map(_.time)
    else if (progressInPerc >= 100d) track.lastOption.map(_.time)
    else (track.headOption, track.lastOption) match {
      case (Some(first), Some(last)) =>
        val millis = interpolate(progressInPerc, first.time.getMillis, last.time.getMillis)
        Some(new DateTime(millis.toLong))
      case _ => None
    }
  }

  /**
   * retrieves a progress between 0 and 100
   */
  def progressForTime(t: DateTime): Double = (track.headOption, track.lastOption) match {
    case (Some(first), Some(last)) => progressForTime(t, first.time, last.time)
    case _ => 0d
  }

  // 0 - 100
  def progressForTime(t: DateTime, first: DateTime, last: DateTime): Double = {
    val millis = t.getMillis
    val firstMillis = first.getMillis
    val lastMillis = last.getMillis
    if (millis <= firstMillis) 0d
    else if (millis >= lastMillis) 100d
    else (millis - firstMillis) * 100 / (lastMillis - firstMillis)
  }

  def sonda(t: DateTime): Sonda = {
    val tn = track.size
    if (tn < 2) Sonda.zeroAt(t)
    else {
      // find the closest track point with a simple binary search
      // eventually to improve the performance by searching on percentage of time between the endpoints of the list
      def findNearestIndex(list: Seq[TrackPoint], t: DateTime, ix: Int): Int = {
        val n = list.size
        if (n < 2) ix
        else {
          val c = n / 2
          val tp = list(c)
          if (t.isBefore(tp.time)) findNearestIndex(list.slice(0, c), t, ix)
          else findNearestIndex(list.slice(c, n), t, ix + c)
        }
      }
      val ix = findNearestIndex(track, t, 0)
      val tr = track(ix)
      val (left, right) = ix match {
        case 0 => (tr, track(1))
        case last if last >= tn - 1 => (track(tn - 2), tr)
        case _ if t.isBefore(tr.time) => (tr, track(ix + 1))
        case _ => (track(ix - 1), tr)
      }
      interpolate(t, left, right).withTrackIndex(ix)
    }
  }

  def interpolate(t: DateTime, left: TrackPoint, right: TrackPoint): Sonda = {
    val f = progressForTime(t, left.time, right.time)
    val elevation = interpolate(f, left.elevation, right.elevation)
    val distance = left.distance + interpolate(f, 0, left.segment)
    Sonda(t,
      InputValue(elevation, elevationBoundary), InputValue(left.grade, gradeBoundary),
      InputValue(distance, MinMax(0, totalDistance)), InputValue(left.speed, speedBoundary)
    )
  }

  def interpolate(f: Double, left: Double, right: Double): Double = left + f * (right - left) / 100
}
