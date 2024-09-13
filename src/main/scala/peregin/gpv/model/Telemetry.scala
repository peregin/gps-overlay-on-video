package peregin.gpv.model

import java.io.{File, InputStream}
import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import peregin.gpv.util.{Io, Logging, SeqUtil, Timed}

import java.time.Instant
import scala.collection.mutable.ArrayBuffer
import scala.math.Ordered.orderingToOrdered
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

  val ALLOW_GPS_GAP_MS = 5_000;

  val elevationBoundary = MinMax.extreme
  val latitudeBoundary = MinMax.extreme
  val longitudeBoundary = MinMax.extreme
  private[model] val speedBoundary = MinMax.extreme
  private[model] val bearingBoundary = MinMax(0, 360)
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
      else if (i > 0) {
        val previous = track(i - 1)
        point.speed = previous.speed
        point.bearing = previous.bearing
        point.grade = previous.grade
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
    val millis = valueForProgress(progressInPerc, (tp: TrackPoint) => tp.time.getMillis.toDouble)
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
    case (Some(first), Some(last)) => progressForValue(t.getMillis.toDouble, first, last, (tp: TrackPoint) => tp.time.getMillis.toDouble)
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

  def sondaForAbsoluteTime(t: DateTime): Sonda = searchTime(t.getMillis.toDouble, (tp: TrackPoint) => tp.time.getMillis.toDouble)

  def sondaForDistance(d: Double): Sonda = search(d, (tp: TrackPoint) => tp.distance)

  // binary search then interpolate
  private def search(t: Double, convertFunc: TrackPoint => Double): Sonda = {
    val previousIdx: Int = math.max(SeqUtil.floorIndex(track, t, convertFunc, java.lang.Double.compare), 0)
    val nextIndex = math.min(previousIdx + 1, track.size - 1)
    val (left, right) = (track(previousIdx), track(nextIndex))
    val progress = progressForValue(t, left, right, convertFunc)
    interpolate(progress, left, right).withTrackIndex(previousIdx)
  }

  // binary search by time, then interpolate
  // returns reasonably empty data if before beginning of track
  private def searchTime(t: Double, convertFunc: TrackPoint => Double): Sonda = {
    var invalid = 0; // 0 is valid, 1 is middle but no GPS, 2 is out of track
    var previousIdx: Int = SeqUtil.floorIndex(track, t, convertFunc, java.lang.Double.compare)
    if (previousIdx < 0) {
      invalid = 2;
      previousIdx = 0;
    }
    else if (t == convertFunc(track(previousIdx))) {
      // ok, at the edge
    }
    else if (previousIdx == track.size - 1) {
      invalid = 2;
    }
    else if (convertFunc(track(previousIdx + 1)) - convertFunc(track(previousIdx)) > ALLOW_GPS_GAP_MS) {
      invalid = 1;
    }
    val nextIndex = math.min(previousIdx + 1, track.size - 1)
    val (left, right) = (track(previousIdx), track(nextIndex))
    val progress = progressForValue(t, left, right, convertFunc)
    val middle = interpolate(progress, left, right).withTrackIndex(previousIdx)
    if (invalid > 0) {
      return Sonda(
        new DateTime(t.toLong),
        InputValue(Some((t.toLong - track.head.time.getMillis) / 1000.0), MinMax(0, (track.last.time.getMillis - track.head.time.getMillis).toDouble)),
        middle.location,
        middle.elevation,
        InputValue(None, gradeBoundary),
        middle.distance,
        InputValue(None, speedBoundary),
        if (invalid == 1) middle.bearing else InputValue(None, bearingBoundary),
        InputValue(None, cadenceBoundary),
        InputValue(None, heartRateBoundary),
        InputValue(None, powerBoundary),
        middle.temperature
      )
        .withTrackIndex(previousIdx)
    }
    else if (previousIdx == 0) {
      return Sonda(
        middle.time,
        middle.elapsedTime,
        middle.location,
        middle.elevation,
        middle.grade,
        middle.distance,
        middle.speed,
        InputValue(None, bearingBoundary),
        middle.cadence,
        middle.heartRate,
        middle.power,
        middle.temperature
      )
        .withTrackIndex(previousIdx)
    }
    else {
      return middle
      .withTrackIndex(previousIdx)
    }
  }

  private def interpolate(progress: Double, left: TrackPoint, right: TrackPoint): Sonda = {
    val t = new DateTime(interpolate(progress, left.time.getMillis.toDouble, right.time.getMillis.toDouble).toLong)
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
    val grade = interpolate(progress, left.grade, right.grade)
    val firstTs = track.head.time.getMillis
    Sonda(t,
      InputValue(Some((t.getMillis - firstTs).toDouble), MinMax(0, (track.last.time.getMillis - firstTs).toDouble)),
      location,
      InputValue(Some(elevation), elevationBoundary),
      InputValue(Some(grade), gradeBoundary),
      InputValue(Some(distance), MinMax(0, totalDistance)),
      InputValue(Some(speed), speedBoundary),
      InputValue(Some(bearing), bearingBoundary),
      InputValue(cadence, cadenceBoundary),
      InputValue(heartRate, heartRateBoundary),
      InputValue(power, powerBoundary),
      InputValue(temperature, temperatureBoundary)
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
