package peregin.gpv.model

import org.specs2.mutable.Specification
import org.joda.time.DateTime


class TelemetrySpec extends Specification {

  "telemetry with 2 track points" should {
    val telemetry = Telemetry(Seq(
      TrackPoint(Telemetry.centerPosition, 100, new DateTime(2014, 6, 1, 10, 0)),
      TrackPoint(Telemetry.centerPosition, 200, new DateTime(2014, 6, 1, 11, 0))
    ))

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
}
