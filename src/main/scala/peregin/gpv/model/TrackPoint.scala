package peregin.gpv.model

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import peregin.gpv.util.TimePrinter
import peregin.gpv.util.Trigo._
import math._


object TrackPoint {

  // Distances from points on the surface to the center range from 6353 km to 6384 km.
  // Several different ways of modeling the Earth as a sphere each yield a mean radius of 6371 kilometers.
  val earthRadius = 6371d

  // The default position shown in the map when no GPS data is loaded.
  // Buerkliplatz, Zurich, Switzerland
  val centerPosition = new GeoPosition(47.366074, 8.541264)

  val millisToHours = 1000 * 60 * 60
}

/**
 * GeoPosition holds the (latitude, longitude) pair, sometimes referred as phi and lambda.
 * Percent slope = %m = (rise / run) * 100
 */
case class TrackPoint(position: GeoPosition,
                      elevation: Double,
                      time: DateTime,
                      extension: GarminExtension) {

  // total distance up to this track point
  var distance = 0d
  // distance between the previous and current track points
  var segment = 0d
  // average speed of travelling form the previous to current track point
  var speed = 0d
  // average grade (expressed in percentage) or steepness of the segment between previous and current track points
  var grade = 0d

  def analyze(next: TrackPoint, prevs: Seq[TrackPoint]): Unit = {
    segment = distanceTo(next)
    next.distance = distance + segment
    val dt = (next.time.getMillis - time.getMillis).toDouble / TrackPoint.millisToHours
    if (dt != 0d) speed = segment / dt
    // smoother grade calculation, consider the previous point as well, otherwise some outliers are being introduced
    val (gradeSegment, firstElevation) = {
      if (prevs.isEmpty) (segment, elevation)
      else (prevs.map(_.segment).sum + segment, prevs.head.elevation)
    }
    if (gradeSegment > 0) grade = (next.elevation - firstElevation) / (gradeSegment * 10)
  }

  // The return value is the distance expressed in kilometers.
  // It uses the haversine formula.
  // The accuracy of the distance is decreasing if:
  // - the points are distant
  // - points are closer to the geographic pole
  def haversineDistanceTo(that: TrackPoint): Double = haversineDistanceTo(that.position)

  def haversineDistanceTo(gp: GeoPosition): Double = {
    val deltaPhi = (position.getLatitude - gp.getLatitude).toRadians
    val deltaLambda = (position.getLongitude - gp.getLongitude).toRadians
    val a = square(sin(deltaPhi / 2)) +
      cos(gp.getLatitude.toRadians) * cos(position.getLatitude.toRadians) * sin(deltaLambda / 2) * sin(deltaLambda / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    TrackPoint.earthRadius * c
  }

  def distanceTo(that: TrackPoint): Double = {
    val d = haversineDistanceTo(that) // km
    val h = (elevation - that.elevation) / 1000 // km
    pythagoras(d, h)
  }

  override def toString = f"${TimePrinter.printTime(time.getMillis)} - [${position.getLatitude}%1.6f,${position.getLongitude}%1.6f] ->$distance%3.2f(\u0394$segment%3.4f) ^$elevation%4.1f %%$grade%2.2f"
}
