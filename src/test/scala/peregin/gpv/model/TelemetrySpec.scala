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

    "interpolate time" in {
      telemetry.progressForTime(new DateTime(2014, 6, 1, 10, 0)) === 0
      telemetry.progressForTime(new DateTime(2014, 6, 1, 10, 30)) === 50
      telemetry.progressForTime(new DateTime(2014, 6, 1, 11, 0)) === 100
    }

    "interpolate elevation" in {
      val sonda = telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 10, 30))
      sonda.time === new DateTime(2014, 6, 1, 10, 30)
      sonda.elevation.current === 150

      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 9, 0)).elevation.current === 100
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 10, 0)).elevation.current === 100
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 11, 0)).elevation.current === 200
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 12, 0)).elevation.current === 200
      telemetry.sondaForAbsoluteTime(new DateTime(2014, 6, 1, 10, 15)).elevation.current === 125
    }

    "interpolate distance" in {
      telemetry.sondaForDistance(-10).distance.current === 0
      telemetry.sondaForDistance(0).distance.current === 0
      telemetry.sondaForDistance(100).distance.current === 100
      telemetry.sondaForDistance(200).distance.current === 200
      telemetry.sondaForDistance(300).distance.current === 200
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
      telemetry.totalDistance === 25.969048381307253
      telemetry.speedBoundary must beCloseTo(MinMax(0.07879420148031871, 86.28724568098714), 6.significantFigures)
      telemetry.gradeBoundary must beCloseTo(MinMax(-35.5112900107015, 38.58484806897635), 6.significantFigures)
      telemetry.cadenceBoundary === MinMax(0, 120)
      telemetry.temperatureBoundary === MinMax(6, 14)
      telemetry.heartRateBoundary === MinMax(104, 175)
    }

    "validate first segment details" in {
      val first = telemetry.track(0)
      first.segment === 0.005317274837638873
      first.speed === 19.142189415499942
      first.grade === 0d
    }

    "find outliers" in {
      val outliers = telemetry.track.count(_.grade > 30)
      log.info(s"found $outliers outliers out of ${telemetry.track.size}")
      outliers === 9
    }
  }

  "telemetry cycling data from Stelvio collected with Strava" should {
    // activity tracked:
    // http://app.strava.com/activities/78985204 - exact data
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/stelvio.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(9558)
      telemetry.elevationBoundary === MinMax(886.0, 2763.0)
      telemetry.speedBoundary.max must beCloseTo(85.56435201871793 within 6.significantFigures)
      telemetry.totalDistance must beCloseTo(63.23256444282121 within 6.significantFigures)
      telemetry.gradeBoundary must beCloseTo(MinMax(-38.71463504215173, 114.48149716420424), 6.significantFigures)
    }
  }

  "telemetry running data from Tuefi track collected with Strava" in {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/track-run.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(1009)
      telemetry.elevationBoundary === MinMax(442.0, 447.0)
      telemetry.speedBoundary.max must beCloseTo(23.656438316953857 within 6.significantFigures)
      telemetry.totalDistance must beCloseTo(4.234620202017025 within 6.significantFigures)
    }
  }

  "telemetry cycling data along Sihl river collected with Garmin 510" should {
    val telemetry = Telemetry.loadWith(XML.load(getClass.getResource("/gps/sample.gpx")))

    "calculate min max" in {
      telemetry.track must haveSize(674)
      telemetry.elevationBoundary === MinMax(452.6, 513.2)
      telemetry.speedBoundary.max must beCloseTo(33.471772761781544 within 6.significantFigures)
      telemetry.totalDistance must beCloseTo(12.492226904069824 within 6.significantFigures)
      telemetry.gradeBoundary must beCloseTo(MinMax(-6.504534982064397, 11.890875118231593), 6.significantFigures)
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
}
