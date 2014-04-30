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

    "interpolate data" in {
      val t = new DateTime(2014, 6, 1, 10, 30)
      val sonda = telemetry.sonda(t)
      sonda.time === t
      sonda.elevation.current === 150
    }
  }
}
