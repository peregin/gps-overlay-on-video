package peregin.gpv.model

import java.io.{File, InputStream}
import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import peregin.gpv.util.{Io, Logging, SeqUtil, Timed}

import scala.annotation.tailrec
import scala.util.Try
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
    val points = (rootNode \ "trk" \ "trkseg" \ "trkpt").flatMap{ node =>
      Try {
        val lat = (node \ "@lat").text.toDouble
        val lon = (node \ "@lon").text.toDouble
        val time = (node \ "time").text
        // some devices are not tracking the elevation, default it 0
        val elevation = Try((node \ "ele").text.toDouble).toOption.getOrElse(0d)
        val extension = node \ "extensions"
        TrackPoint(
          new GeoPosition(lat, lon), elevation,
          DateTime.parse(time), GarminExtension.parse(extension)
        )
      }.toOption
    }.toVector

    // Important: points are stored in a <b>Vector</b>, allows to efficiently access an element at an arbitrary position
    // and retrieving the size is O(1)

    loadWith(points)
  }

  def loadWith(track: Seq[TrackPoint]): Telemetry = {
    log.info(s"found ${track.size} track points")
    val data = new Telemetry(track)
    data.analyze()
    log.info(f"elevation boundary: ${data.elevationBoundary}, max speed: ${data.speedBoundary.max}%.02f")
    data
  }

  def empty() = new Telemetry(Seq.empty)

  def sample(): Telemetry = timed("load sample gps data") {
    Try(Telemetry.load(Io.getResource("gps/sample.gpx")))
      .toOption
      .getOrElse(Telemetry.empty())
  }
}

case class Telemetry(track: Seq[TrackPoint]) extends Timed with Logging {

  val elevationBoundary = MinMax.extreme
  val latitudeBoundary = MinMax.extreme
  val longitudeBoundary = MinMax.extreme
  private[model] val speedBoundary = MinMax.extreme
  private[model] val bearingBoundary = MinMax.extreme
  val gradeBoundary = MinMax.extreme
  private[model] val cadenceBoundary = MinMax.extreme
  private[model] val temperatureBoundary = MinMax.extreme
  private[model] val heartRateBoundary = MinMax.extreme
  private[model] val powerBoundary = MinMax.extreme

  private var centerPosition = TrackPoint.centerPosition

  private def analyze(): Unit = timed("analyze GPS data") {
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
        val prevPoints = track.slice(0.max(i - 10), 0.max(i))
        point.analyze(nextPoint, prevPoints)
        speedBoundary.sample(point.speed)
        bearingBoundary.sample(point.bearing)
        gradeBoundary.sample(point.grade)
      }
    }
    centerPosition = new GeoPosition(latitudeBoundary.mean, longitudeBoundary.mean)
  }

  def centerGeoPosition: GeoPosition = centerPosition

  def minTime: DateTime = track.head.time
  def maxTime: DateTime = track.last.time

  def totalDistance: Double = track.lastOption.map(_.distance).getOrElse(0d)

  /**
   * Retrieves exact track point for the given progress.
   *
   * @param progressInPerc
   *      progress in track
   *
   * @return
   *      track point for the given progress.
   */
  def pointForProgress(progressInPerc: Double): TrackPoint = {
    val index = progressInPerc * track.size / 100
    return track(math.min(index.toInt, track.size - 1))
  }

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
  private def valueForProgress(progressInPerc: Double, convertFunc: TrackPoint => Double): Option[Double] = {
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
  private def progressForValue(v: Double, first: TrackPoint, last: TrackPoint, convertFunc: TrackPoint => Double): Double = {
    val firstV = convertFunc(first)
    val lastV = convertFunc(last)
    if (v <= firstV) 0d
    else if (v >= lastV) 100d
    else if (lastV == firstV) 0d
    else (v - firstV) * 100 / (lastV - firstV)
  }


  // convenience methods to retrieve a point from the track (based on time, distance, gepo position, etc.)

  def sondaForRelativeTime(tsInMillis: Long): Option[Sonda] = {
    if (track.isEmpty) None
    else Some(sondaForAbsoluteTime(track.head.time.plusMillis(tsInMillis.toInt)))
  }

  def sondaForPosition(gp: GeoPosition): Option[Sonda] = {
    import Ordering.Double.TotalOrdering
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
  private def search(t: Double, convertFunc: TrackPoint => Double): Sonda = {
    val previousIdx: Int = math.max(SeqUtil.floorIndex(track, t, convertFunc, java.lang.Double.compare), 0)
    val nextIndex = math.min(previousIdx + 1, track.size - 1)
    val (left, right) = (track(previousIdx), track(nextIndex))
    val progress = progressForValue(t, left, right, convertFunc)
    interpolate(progress, left, right).withTrackIndex(previousIdx)
  }

  private def interpolate(progress: Double, left: TrackPoint, right: TrackPoint): Sonda = {
    val t = new DateTime(interpolate(progress, left.time.getMillis, right.time.getMillis).toLong)
    val elevation = interpolate(progress, left.elevation, right.elevation)
    val distance = interpolate(progress, left.distance, right.distance)
    val location = new GeoPosition(
      interpolate(progress, left.position.getLatitude, right.position.getLatitude),
      interpolateCircular(progress, left.position.getLongitude, right.position.getLongitude, TrackPoint.longitudeRange)
    )
    val bearing = interpolateCircular(progress, left.bearing, right.bearing, TrackPoint.azimuthRange)
    val speed = interpolate(progress, left.speed, right.speed)
    val cadence = interpolate(progress, left.extension.cadence, right.extension.cadence)
    val heartRate = interpolate(progress, left.extension.heartRate, right.extension.heartRate)
    val power = interpolate(progress, left.extension.power, right.extension.power)
    val temperature = interpolate(progress, left.extension.temperature, right.extension.temperature)
    val firstTs = track.head.time.getMillis
    Sonda(t, InputValue(t.getMillis - firstTs, MinMax(0, track.last.time.getMillis - firstTs)),
      location,
      InputValue(elevation, elevationBoundary), InputValue(left.grade, gradeBoundary),
      InputValue(distance, MinMax(0, totalDistance)), InputValue(speed, speedBoundary), InputValue(bearing, bearingBoundary),
      cadence.map(InputValue(_, cadenceBoundary)),
      heartRate.map(InputValue(_, heartRateBoundary)),
      power.map(InputValue(_, powerBoundary)),
      temperature.map(InputValue(_, temperatureBoundary))
    )
  }

  private def interpolate(f: Double, left: Double, right: Double): Double = left + f * (right - left) / 100

  private def interpolate(f: Double, oLeft: Option[Double], oRight: Option[Double]): Option[Double] = (oLeft, oRight) match {
    case (Some(l), Some(r)) => Some(interpolate(f, l, r))
    case _ => None
  }

  /**
   * Interpolates data in circular unit, such as bearing (0 - 360) or longitude (-180 - +180)
   */
  private def interpolateCircular(f: Double, left: Double, right: Double, range: MinMax): Double = {
    val rangeFull = range.max - range.min
    val rangeHalf = rangeFull / 2
    var diff = right - left
    if (diff < -rangeHalf) {
      diff += rangeFull
    }
    else if (diff >= rangeHalf) {
      diff -= rangeFull
    }
    var result = left + f * diff / 100
    if (result < range.min) {
      result += rangeFull
    }
    else if (result >= range.max) {
      result -= rangeFull
    }
    result
  }

}
