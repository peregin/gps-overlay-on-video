package peregin.gpv.model

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime

object TrackPoint {

  // Distances from points on the surface to the center range from 6353 km to 6384 km.
  // Several different ways of modeling the Earth as a sphere each yield a mean radius of 6371 kilometers.
  val earthRadius = 6371d

  // The default position shown in the map when no GPS data is loaded.
  // Buerkliplatz, Zurich, Switzerland
  val centerPosition = new GeoPosition(47.366074, 8.541264)
}

/**
 * GeoPosition holds the (latitude, longitude) pair, sometimes referred as phi and lambda.
 * Percent slope = %m = (rise / run) * 100
 */
case class TrackPoint(position: GeoPosition,
                      elevation: Double,
                      time: DateTime) {

  // total distance up to this track point
  var distance = 0d
  // distance between the previous and current track points
  var segment = 0d
  // average speed of travelling form the previous to current track point
  var speed = 0d
  // average grade (expressed in percentage) or steepness of the segment between previous and current track points
  var grade = 0d

  val millisToHours = 1000 * 60 * 60

  def analyze(next: TrackPoint) {
    segment = distanceTo(next)
    next.distance = distance + segment
    val dt = (next.time.getMillis - time.getMillis).toDouble / millisToHours
    if (dt != 0d) speed = segment / dt
    if (segment > 0) grade = (next.elevation - elevation) / (segment * 10)
  }

  // The return value is the distance expressed in kilometers.
  // It uses a flat surface formula, spherical earth projected to a plane with the pythagorean theorem.
  // The accuracy of the distance is decreasing if:
  // - the points are distant
  // - points are closer to the geographic pole
  def flatDistanceTo(that: TrackPoint): Double = {
    val deltaPhi = (position.getLatitude - that.position.getLatitude).toRadians
    val deltaLambda = (position.getLongitude - that.position.getLongitude).toRadians
    val phiMean = (position.getLatitude + that.position.getLatitude).toRadians / 2
    import math._
    TrackPoint.earthRadius * sqrt(square(deltaPhi) + square(cos(phiMean) * square(deltaLambda)))
  }

  def distanceTo(that: TrackPoint): Double = {
    val d = flatDistanceTo(that)
    val h = elevation - that.elevation
    import math._
    sqrt(square(d) + square(h))
  }

  def square(v: Double) = v * v
}
