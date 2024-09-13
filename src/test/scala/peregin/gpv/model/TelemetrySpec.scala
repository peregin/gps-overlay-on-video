package peregin.gpv.model

import org.specs2.mutable.Specification
import org.joda.time.{DateTime, DateTimeZone}
import org.specs2.matcher.{Expectable, MatchResult, Matcher, SignificantFigures}
import peregin.gpv.util.Logging

import scala.xml.XML


class TelemetrySpec extends Specification with Logging {

  def beCloseTo(ref: MinMax, figures: SignificantFigures): Matcher[MinMax] = new Matcher[MinMax] {
    def apply[T <: MinMax](e: Expectable[T]): MatchResult[T] = {
      val res1 = e.value.min must beCloseTo(ref.min within figures).updateMessage(s => s"min value $s")
      val res2 = e.value.max must beCloseTo(ref.max within figures).updateMessage(s => s"max value $s")
      result(res1 and res2, e)
    }
  }

  "telemetry data with 2 track points" should {
    val track = Seq(
      TrackPoint(TrackPoint.centerPosition, 100, new DateTime(2014, 6, 1, 10, 0), GarminExtension(Some(72), Some(12), Some(110), None)),
      TrackPoint(TrackPoint.centerPosition, 200, new DateTime(2014, 6, 1, 11, 0), GarminExtension(Some(81), Some(14), Some(120), None))
    )
    track(0).distance = 0
    track(0).segment = 200
    track(1).distance = 200
    track(1).segment = 180
    val telemetry = Telemetry(track)

    "copy to last point" in {
      telemetry.track.last.speed === telemetry.track(telemetry.track.size - 2).speed
      telemetry.track.last.bearing === telemetry.track(telemetry.track.size - 2).bearing
      telemetry.track.last.grade === telemetry.track(telemetry.track.size - 2).grade
    }

    "interpolate time" in {
      telemetry.progressForTime(new DateTime(2014, 6, 1, 10, 0)) === 0
      telemetry.progressForTime(new DateTime(2014, 6, 1, 10, 30)) === 50
      telemetry.progressForTime(new DateTime(2014, 6, 1, 11, 0)) === 100
    }

    "interpolate elevation" in {
      val sonda = telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 10, 30))
      sonda.time === new DateTime(2014, 6, 1, 10, 30)
      sonda.elevation.current.get === 150

      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 9, 0)).elevation.current.get === 100
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 10, 0)).elevation.current.get === 100
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 11, 0)).elevation.current.get === 200
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 12, 0)).elevation.current.get === 200
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 10, 15)).elevation.current.get === 125
    }

    "interpolate distance" in {
      telemetry.sondaForDistance(-10).distance.current.get === 0
      telemetry.sondaForDistance(0).distance.current.get === 0
      telemetry.sondaForDistance(100).distance.current.get === 100
      telemetry.sondaForDistance(200).distance.current.get === 200
      telemetry.sondaForDistance(300).distance.current.get === 200
    }
  }

  "telemetry with holes" should {
    val telemetry = TelemetryTestUtil.buildTelemetry(
      0, 0,
      10, 10,
      -10, 20,
    )

    "bearing ignored for first point" in {
      val sonda = telemetry.sondaForRelativeTime(500)
      sonda.get.bearing.current.isDefined === false
    }

    "data empty before start" in {
      val sonda = telemetry.sondaForRelativeTime(-500)
      sonda.get.location !== null
      sonda.get.elevation.current.isDefined === true
      sonda.get.grade.current.isDefined === false
      sonda.get.distance.current.isDefined === true
      sonda.get.speed.current.isDefined === false
      sonda.get.bearing.current.isDefined === false
      sonda.get.cadence.current.isDefined === false
      sonda.get.heartRate.current.isDefined === false
      sonda.get.power.current.isDefined === false
      sonda.get.temperature.current.isDefined === true
    }

    "data empty after end" in {
      val sonda = telemetry.sondaForRelativeTime(2_500)
      sonda.get.location !== null
      sonda.get.elevation.current.isDefined === true
      sonda.get.grade.current.isDefined === false
      sonda.get.distance.current.isDefined === true
      sonda.get.speed.current.isDefined === false
      sonda.get.bearing.current.isDefined === false
      sonda.get.cadence.current.isDefined === false
      sonda.get.heartRate.current.isDefined === false
      sonda.get.power.current.isDefined === false
      sonda.get.temperature.current.isDefined === true
    }
  }

  "telemetry data cycling from Sihlwald collected with Garmin" should {
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
      telemetry.totalDistance must beCloseTo(25.99097579963173, 6.significantFigures)
      telemetry.speedBoundary must beCloseTo(MinMax(0.09113679892517283, 59.29239119878211), 6.significantFigures)
      telemetry.gradeBoundary must beCloseTo(MinMax(-98.29878158370276, 82.98222101329881), 6.significantFigures)
      telemetry.cadenceBoundary === MinMax(0, 120)
      telemetry.temperatureBoundary === MinMax(6, 14)
      telemetry.heartRateBoundary === MinMax(104, 175)
    }

    "validate first segment details" in {
      val first = telemetry.track(0)
      first.segment must beCloseTo(0.005316972239356387, 6.significantFigures)
      first.speed === 19.14110006168299
      first.grade === 0d
    }

    "find outliers" in {
      val outliers = telemetry.track.count(_.grade > 30)
      log.info(s"found $outliers outliers out of ${telemetry.track.size}")
      outliers === 24
    }
  }

  "telemetry cycling data from Stelvio collected with Strava" should {
    // activity tracked:
    // http://app.strava.com/activities/78985204 - exact data
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/stelvio.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(9558)
      telemetry.elevationBoundary === MinMax(886.0, 2763.0)
      telemetry.speedBoundary.max must beCloseTo(72.51322702575003 within 6.significantFigures)
      telemetry.totalDistance must beCloseTo(63.299609521916814 within 6.significantFigures)
      telemetry.gradeBoundary must beCloseTo(MinMax(-38.887448712336536, 45.433599295298116), 6.significantFigures)
    }
  }

  "telemetry running data from Tuefi track collected with Strava" in {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/track-run.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(1009)
      telemetry.elevationBoundary === MinMax(442.0, 447.0)
      telemetry.speedBoundary.max must beCloseTo(22.05241079088749 within 6.significantFigures)
      telemetry.totalDistance must beCloseTo(4.239035087385639 within 6.significantFigures)
    }
  }

  "telemetry cycling data along Sihl river collected with Garmin 510" should {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/sample.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(674)
//      System.out.println(telemetry.track.map(tp =>
//        "" + tp.time + "\t" + tp.distance + "\t" + tp.elevation + "\t" + tp.grade + "\t" + tp.speed
//      ))
      telemetry.elevationBoundary === MinMax(452.6, 513.2)
      telemetry.speedBoundary.max must beCloseTo(31.159996504826278 within 6.significantFigures)
      telemetry.totalDistance must beCloseTo(12.502044792928476 within 6.significantFigures)
      telemetry.gradeBoundary must beCloseTo(MinMax(-17.13448107713459, 20.26293073285657), 6.significantFigures)
    }
  }

  "telemetry cycling data with power meter and heart rate from Iron Bike Einsiedeln 2017 and Garmin Edge 820" should {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/power-and-heart.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(64)
      telemetry.heartRateBoundary === MinMax(160, 169)
      telemetry.powerBoundary === MinMax(5, 363)
      telemetry.track.head.extension.power must beSome(205d)
      telemetry.track.head.extension.heartRate must beSome(160d)
    }
  }

  "telemetry data having a few trackpoints without elevation nodes" should {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/20190517_221037.gpx")))

    "be parsed" in {
      telemetry.track must haveSize(2522)
    }
  }

  "telemetry data without any elevation" should {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/noelevation.gpx")))

    "be parsed" in {
      telemetry.track must haveSize(179)
    }
  }

  "bearing crosses north" should {
    val telemetry = TelemetryTestUtil.buildTelemetry(
      0, 0,
      0, 10,
      10, 10,
      20, 11,
      0, 10,
      10, 10,
      20, 9,
    )

    "interpolate cross to east" in {
      val sonda = telemetry.sondaForRelativeTime(1_750).get
      sonda.bearing.current.get must beCloseTo(4.0715656127530515 within 6.significantFigures)
    }

    "interpolate cross to west" in {
      val sonda = telemetry.sondaForRelativeTime(4_750).get
      sonda.bearing.current.get must beCloseTo(355.92843438724697 within 6.significantFigures)
    }
  }

  "longitude crosses 180" should {
    val telemetry = TelemetryTestUtil.buildTelemetry(
      0, 170,
      10, 170,
      10, -170,
      10, -170,
      10, 170,
    )

    "interpolate cross to east" in {
      val sonda = telemetry.sondaForRelativeTime(1_750).get
      sonda.location.getLongitude must beCloseTo(-175.0 within 6.significantFigures)
    }

    "interpolate cross to west" in {
      val sonda = telemetry.sondaForRelativeTime(3_750).get
      sonda.location.getLongitude must beCloseTo(175.0 within 6.significantFigures)
    }
  }
}
