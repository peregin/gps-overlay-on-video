package peregin.gpv.gui.gauge

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import peregin.gpv.model.{GarminExtension, Telemetry, TrackPoint}

import scala.collection.mutable.ArrayBuffer

class MapGaugeSpec extends Specification {
  stopOnFail

  "crossing equator" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      -80, -80,
      +80, +80,
    )

    "scaleLongitude is 1" in {
      mapGauge.scaleLongitude must beCloseTo(1.0 within 6.significantFigures)
    }
    "screenPositions are even" in {
      mapGauge.latitudeToScreen(-40, 10) must beCloseTo(400, 1)
      mapGauge.longitudeToScreen(-40, 10) must beCloseTo(400, 1)
    }
  }

  "low latitude" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      +10, -80,
      +80, +80,
    )

    "scaleLongitude is high" in {
      mapGauge.scaleLongitude must beCloseTo(0.98480775301 within 6.significantFigures)
    }

    "screenPositions are lightly skewed" in {
      mapGauge.latitudeToScreen(45, 10) must beCloseTo(350, 1)
      mapGauge.longitudeToScreen(-40, 10) must beCloseTo(393, 1)
    }
  }

  "pole latitude" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      +80, 80,
      +88, 88,
    )

    "scaleLongitude is low" in {
      mapGauge.scaleLongitude must beCloseTo(0.17364817766 within 6.significantFigures)
    }

    "screenPositions are heavily skewed" in {
      mapGauge.latitudeToScreen(82, 125) must beCloseTo(250, 1)
      mapGauge.longitudeToScreen(82, 125) must beCloseTo(43, 1)
    }
  }

  "constant longitude" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      +0, 10,
      +10, 10,
    )

    "does not crash" in {
      mapGauge.latitudeToScreen(5, 1) must beCloseTo(0, 1000)
      mapGauge.longitudeToScreen(10, 1) must beCloseTo(0, 1000)
    }
  }

  "constant latitude" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      +0, 0,
      +0, 10,
    )

    "does not crash" in {
      mapGauge.latitudeToScreen(0, 1) must beCloseTo(0, 1000)
      mapGauge.longitudeToScreen(5, 1) must beCloseTo(0, 1000)
    }
  }

  "single point" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      +0, 0,
    )

    "does not crash" in {
      mapGauge.latitudeToScreen(0, 1) must beCloseTo(0, 1000)
      mapGauge.longitudeToScreen(5, 1) must beCloseTo(0, 1000)
    }
  }

  "crossing longitude 180" should {
    val mapGauge: MapGauge = new MapGauge()
    mapGauge.telemetry = buildTelemetry(
      +10, 170,
      +30, -170,
    )

    "scaleLongitude is based on difference 20" in {
      mapGauge.scaleLongitude must beCloseTo(0.98480775301 within 6.significantFigures)
    }

    "screenPositions are based on difference 20, 20" in {
      mapGauge.latitudeToScreen(15, 50) must beCloseTo(250, 1)
      mapGauge.longitudeToScreen(175, 50) must beCloseTo(246, 1)
    }
  }

  private def buildTelemetry(positions: Double*): Telemetry = {
    val telemetry: Telemetry = Telemetry.loadWith(buildTrack(positions: _*))
    telemetry
  }

  private def buildTrack(positions: Double*): Seq[TrackPoint] = {
    val tps: ArrayBuffer[TrackPoint] = new ArrayBuffer[TrackPoint];
    for (i <- 0 until positions.length by 2) {
      tps.addOne(TrackPoint(new GeoPosition(positions(i + 0), positions(i + 1)), 0, new DateTime(i * 1000L), GarminExtension.empty))
    }
    tps.toSeq
  }
}
