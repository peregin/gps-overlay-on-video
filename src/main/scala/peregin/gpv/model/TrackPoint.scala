package peregin.gpv.model

import org.geotools.referencing.GeodeticCalculator
import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import peregin.gpv.util.{SeqUtil, TimePrinter}
import peregin.gpv.util.Trigo._

import math._


object TrackPoint {
  // The default position shown in the map when no GPS data is loaded.
  // Buerkliplatz, Zurich, Switzerland
  val centerPosition = new GeoPosition(47.366074, 8.541264)

  val millisToHours = 1000 * 60 * 60

  val longitudeRange = MinMax(-180, +180)
  val azimuthRange = MinMax(0, 360)
}

/**
 * GeoPosition holds the (latitude, longitude) pair, sometimes referred as phi and lambda.
 * Percent slope = %m = (rise / run) * 100
 */
case class TrackPoint(position: GeoPosition,
                      elevation: Double,
                      time: DateTime,
                      extension: GarminExtension) {

  // total distance in kilometers up to this track point
  var distance = 0d
  // distance between the previous and current track points
  var segment = 0d
  // average speed of travelling form the previous to current track point
  var speed = 0d
  // average bearing from the previous to current track point
  var bearing = 0d
  // average grade (expressed in percentage) or steepness of the segment between previous and current track points
  var grade = 0d

  def analyze(next: TrackPoint, prevs: Seq[TrackPoint]): Unit = {
    segment = distanceTo(next)
    next.distance = distance + segment

    // smoother speed calculation, consider up to 3 seconds back and 1 point forward:
    val firstSpeed: TrackPoint = SeqUtil.findReverseTill(prevs, (p: TrackPoint) => this.time.getMillis - p.time.getMillis <= 3000).getOrElse(this)
    val dt = (next.time.getMillis - firstSpeed.time.getMillis).toDouble / TrackPoint.millisToHours
    if (dt != 0d) speed = (next.distance - firstSpeed.distance) / dt

    // smoother grade calculation, consider up to 9 seconds back and 1 point forward:
    val firstElevation: TrackPoint = SeqUtil.findReverseTill(prevs, (p: TrackPoint) => this.time.getMillis - p.time.getMillis <= 9000).getOrElse(this)
    val distanceElevation = next.distance - firstElevation.distance
    if (distanceElevation > 0) grade = (next.elevation - firstElevation.elevation) / distanceElevation * (100 / 1000.0)

    bearing = bearingTo(next)
  }

  // The return value is the distance expressed in kilometers.
  // It uses the haversine formula.
  // The accuracy of the distance is decreasing if:
  // - the points are distant
  // - points are closer to the geographic pole
  def haversineDistanceTo(that: TrackPoint): Double = haversineDistanceTo(that.position)

  def haversineDistanceTo(gp: GeoPosition): Double = {
    val calculator = new GeodeticCalculator();

    calculator.setStartingGeographicPoint(this.position.getLongitude, this.position.getLatitude);
    calculator.setDestinationGeographicPoint(gp.getLongitude, gp.getLatitude);

    calculator.getOrthodromicDistance / 1000.0
  }

  def bearingTo(that: TrackPoint): Double = bearingTo(that.position)

  def bearingTo(gp: GeoPosition): Double = {
    val calculator = new GeodeticCalculator();

    calculator.setStartingGeographicPoint(this.position.getLongitude, this.position.getLatitude);
    calculator.setDestinationGeographicPoint(gp.getLongitude, gp.getLatitude);

    var azimuth = calculator.getAzimuth(); // This returns azimuth in degrees (clockwise from north)
    if (azimuth < 0) {
      azimuth += 360.0;
    }
    azimuth
  }

  def distanceTo(that: TrackPoint): Double = {
    val d = haversineDistanceTo(that) // km
    val h = (elevation - that.elevation) / 1000 // km
    pythagoras(d, h)
  }

  override def toString = f"${TimePrinter.printTime(time.getMillis)} - [${position.getLatitude}%1.6f,${position.getLongitude}%1.6f] ->$distance%3.2f(\u0394$segment%3.4f) ^$elevation%4.1f %%$grade%2.2f"
}
