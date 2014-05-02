package peregin.gpv.model

import org.specs2.mutable.Specification
import org.joda.time.{DateTimeZone, DateTime}
import scala.xml.XML


class TelemetrySpec extends Specification {

  "telemetry with 2 track points" should {
    val track = Seq(
      TrackPoint(TrackPoint.centerPosition, 100, new DateTime(2014, 6, 1, 10, 0)),
      TrackPoint(TrackPoint.centerPosition, 200, new DateTime(2014, 6, 1, 11, 0))
    )
    val telemetry = Telemetry(track)

    "interpolate time" in {
      telemetry.progressForTime(new DateTime(2014, 6, 1, 10, 0)) === 0
      telemetry.progressForTime(new DateTime(2014, 6, 1, 10, 30)) === 50
      telemetry.progressForTime(new DateTime(2014, 6, 1, 11, 0)) === 100
    }

    "interpolate elevation" in {
      val sonda = telemetry.sonda(new DateTime(2014, 6, 1, 10, 30))
      sonda.time === new DateTime(2014, 6, 1, 10, 30)
      sonda.elevation.current === 150

      telemetry.sonda(new DateTime(2014, 6, 1, 9, 0)).elevation.current === 100
      telemetry.sonda(new DateTime(2014, 6, 1, 10, 0)).elevation.current === 100
      telemetry.sonda(new DateTime(2014, 6, 1, 11, 0)).elevation.current === 200
      telemetry.sonda(new DateTime(2014, 6, 1, 12, 0)).elevation.current === 200
      telemetry.sonda(new DateTime(2014, 6, 1, 10, 15)).elevation.current === 125
    }
  }

  "telemetry with test data from Sihlwald" should {

    // activity tracked:
    // http://connect.garmin.com/activity/491279898 - extact data
    // http://app.strava.com/activities/127544825 - contains more data
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/sihlwald.gpx")))

    "calculate telemetry data min max" in {
      telemetry.track must haveSize(2219)
      val tz = DateTimeZone.forID("Europe/Zurich")
      telemetry.minTime.getMillis === new DateTime(2014, 4, 6, 10, 6, 21, tz).getMillis
      telemetry.maxTime.getMillis === new DateTime(2014, 4, 6, 12, 6, 26, tz).getMillis
      telemetry.elevationBoundary === MinMax(446.2, 913.2)
      telemetry.latitudeBoundary === MinMax(47.231995, 47.310311)
      telemetry.longitudeBoundary === MinMax(8.504216, 8.566166)
      telemetry.totalDistance === 25.969048381307253
      telemetry.speedBoundary === MinMax(0d, 59.24465691454863)
      //telemetry.gradeBoundary === MinMax(0d, 59.24465691454863)
    }

    "validate first segment details" in {
      val first = telemetry.track(0)
      first.segment === 0.005317274837638873
      first.speed === 18.814181589111467
      first.grade === 0d
    }
  }
}
