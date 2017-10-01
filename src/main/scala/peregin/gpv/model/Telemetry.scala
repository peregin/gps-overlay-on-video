package peregin.gpv.model

import java.io.{File, InputStream}

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import peregin.gpv.util.{Logging, Timed}

import scala.xml.{Node, XML}

/**
  * Extracts telemetry data from a GPX file
  *
  * A sample in a segment looks like:
  * <code>
  *
  *      <trkpt lat="47.1512900" lon="8.7887940">
  *        <ele>902.4</ele>
  *        <time>2017-09-24T06:10:53Z</time>
  *        <extensions>
  *         <power>205</power>
  *         <gpxtpx:TrackPointExtension>
  *          <gpxtpx:atemp>8</gpxtpx:atemp>
  *          <gpxtpx:hr>160</gpxtpx:hr>
  *          <gpxtpx:cad>90</gpxtpx:cad>
  *         </gpxtpx:TrackPointExtension>
  *        </extensions>
  *       </trkpt>
  *
  * </code>
  */
object Telemetry extends Timed with Logging {

  def load(file: File): Telemetry = loadWith(XML.loadFile(file))

  def load(is: InputStream): Telemetry = loadWith(XML.load(is))

  def loadWith(loadFunc: => Node): Telemetry = timed("load telemetry") {
    val rootNode = loadFunc
    val points = (rootNode \ "trk" \ "trkseg" \ "trkpt").map{ node =>
      val lat = (node \ "@lat").text.toDouble
      val lon = (node \ "@lon").text.toDouble
      val time = (node \ "time").text
      val elevation = (node \ "ele").text.toDouble
      val extension = (node \ "extensions")
      TrackPoint(new GeoPosition(lat, lon), elevation,
        DateTime.parse(time), GarminExtension.parse(extension))
    }.toVector

    // Important: points are stored in a <b>Vector</b>, allows to efficiently access an element at an arbitrary position
    // and retrieving the size is O(1)

    log.info(s"found ${points.size} track points")
    val data = new Telemetry(points)
    data.analyze()
    log.info(s"elevation boundary ${data.elevationBoundary}")
    data
  }

  def empty = new Telemetry(Seq.empty)
}

case class Telemetry(track: Seq[TrackPoint]) extends Timed with Logging {

  val elevationBoundary = MinMax.extreme
  val latitudeBoundary = MinMax.extreme
  val longitudeBoundary = MinMax.extreme
  val speedBoundary = MinMax.extreme
  val gradeBoundary = MinMax.extreme
  val cadenceBoundary = MinMax.extreme
  val temperatureBoundary = MinMax.extreme
  val heartRateBoundary = MinMax.extreme
  val powerBoundary = MinMax.extreme

  private var centerPosition = TrackPoint.centerPosition

  def analyze() = timed("analyze GPS data") {
    val n = track.size
    for (i <- 0 until n) {
      val point = track(i)
      elevationBoundary.sample(point.elevation)
      latitudeBoundary.sample(point.position.getLatitude)
      longitudeBoundary.sample(point.position.getLongitude)
      point.extension.cadence.foreach(cadenceBoundary.sample)
      point.extension.temperature.foreach(temperatureBoundary.sample)
      point.extension.heartRate.foreach(heartRateBoundary.sample)
      point.extension.power.foreach(powerBoundary.sample)
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
   * Retrieves the interpolated distance for the given progress.
   */
  def distanceForProgress(progressInPerc: Double): Option[Double] = {
    valueForProgress(progressInPerc, (tp: TrackPoint) => tp.distance)
  }
  
  /**
   * Retrieves the interpolated time for the given progress.
   */
  def timeForProgress(progressInPerc: Double): Option[DateTime] = {
    val millis = valueForProgress(progressInPerc, (tp: TrackPoint) => tp.time.getMillis)
    millis.map(v => new DateTime(v.toLong))
  }

  /**
   * Convenience method to find a given value/field belonging to the track point based on the actual progress.
   * @param progressInPerc is defined between 0 and 100
   */
  private def valueForProgress(progressInPerc: Double, convertFunc: (TrackPoint) => Double): Option[Double] = {
    if (progressInPerc <= 0d) track.headOption.map(convertFunc)
    else if (progressInPerc >= 100d) track.lastOption.map(convertFunc)
    else (track.headOption, track.lastOption) match {
      case (Some(first), Some(last)) => Some(interpolate(progressInPerc, convertFunc(first), convertFunc(last)))
      case _ => None
    }
  }

  /**
   * Retrieves a progress between 0 and 100 according to the time elapsed.
   */
  def progressForTime(t: DateTime): Double = (track.headOption, track.lastOption) match {
    case (Some(first), Some(last)) => progressForValue(t.getMillis, first, last, (tp: TrackPoint) => tp.time.getMillis)
    case _ => 0d
  }

  /**
   * Retrieves a progress between 0 and 100 according to the actual distance.
   */
  def progressForDistance(d: Double): Double = (track.headOption, track.lastOption) match {
    case (Some(first), Some(last)) => progressForValue(d, first, last, (tp: TrackPoint) => tp.distance)
    case _ => 0d
  }

  // 0 - 100
  private def progressForValue(v: Double, first: TrackPoint, last: TrackPoint, convertFunc: (TrackPoint) => Double): Double = {
    val firstV = convertFunc(first)
    val lastV = convertFunc(last)
    if (v <= firstV) 0d
    else if (v >= lastV) 100d
    else (v - firstV) * 100 / (lastV - firstV)
  }


  // convenience methods to retrieve a point from the track (based on time, distance, gepo position, etc.)

  def sondaForRelativeTime(tsInMillis: Long): Option[Sonda] = {
    if (track.isEmpty) None
    else Some(sondaForAbsoluteTime(track.head.time.plusMillis(tsInMillis.toInt)))
  }

  def sondaForPosition(gp: GeoPosition): Option[Sonda] = {
    if (track.size < 3) None
    else {
      // drop first and last where the distance is incorrect
      val dList = track.map(t => (t.haversineDistanceTo(gp), t))
      val tp = dList.minBy(_._1)._2
      Some(sondaForAbsoluteTime(tp.time))
    }
  }

  def sondaForAbsoluteTime(t: DateTime): Sonda = search(t.getMillis, (tp: TrackPoint) => tp.time.getMillis)

  def sondaForDistance(d: Double): Sonda = search(d, (tp: TrackPoint) => tp.distance)

  // binary search then interpolate
  private def search(t: Double, convertFunc: (TrackPoint) => Double): Sonda = {
    val tn = track.size
    if (tn < 2) Sonda.empty
    else {
      // find the closest track point with a simple binary search
      // eventually to improve the performance by searching on percentage of time between the endpoints of the list
      def findNearestIndex(list: Seq[TrackPoint], t: Double, ix: Int): Int = {
        val n = list.size
        if (n < 2) ix
        else {
          val c = n / 2
          val tp = list(c)
          if (t < convertFunc(tp)) findNearestIndex(list.slice(0, c), t, ix)
          else findNearestIndex(list.slice(c, n), t, ix + c)
        }
      }
      val ix = findNearestIndex(track, t, 0)
      val tr = track(ix)
      val (left, right) = ix match {
        case 0 => (tr, track(1))
        case last if last >= tn - 1 => (track(tn - 2), tr)
        case _ if t < convertFunc(tr) => (tr, track(ix + 1))
        case _ => (track(ix - 1), tr)
      }
      val progress = progressForValue(t, left, right, convertFunc)
      interpolate(progress, left, right).withTrackIndex(ix)
    }
  }

  private def interpolate(progress: Double, left: TrackPoint, right: TrackPoint): Sonda = {
    val t = new DateTime(interpolate(progress, left.time.getMillis, right.time.getMillis).toLong)
    val elevation = interpolate(progress, left.elevation, right.elevation)
    val distance = left.distance + interpolate(progress, 0, left.segment)
    val location = new GeoPosition(
      interpolate(progress, left.position.getLatitude, right.position.getLatitude),
      interpolate(progress, left.position.getLongitude, right.position.getLongitude)
    )
    val cadence = interpolate(progress, left.extension.cadence, right.extension.cadence)
    val heartRate = interpolate(progress, left.extension.heartRate, right.extension.heartRate)
    val firstTs = track.head.time.getMillis
    Sonda(t, InputValue(t.getMillis - firstTs, MinMax(0, track.last.time.getMillis - firstTs)),
      location,
      InputValue(elevation, elevationBoundary), InputValue(left.grade, gradeBoundary),
      InputValue(distance, MinMax(0, totalDistance)), InputValue(left.speed, speedBoundary),
      cadence.map(InputValue(_, cadenceBoundary)), heartRate.map(InputValue(_, heartRateBoundary))
    )
  }

  def interpolate(f: Double, left: Double, right: Double): Double = left + f * (right - left) / 100

  def interpolate(f: Double, oLeft: Option[Double], oRight: Option[Double]): Option[Double] = (oLeft, oRight) match {
    case (Some(l), Some(r)) => Some(interpolate(f, l, r))
    case _ => None
  }
}
