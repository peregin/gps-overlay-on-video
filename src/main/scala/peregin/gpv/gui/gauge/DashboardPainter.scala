package peregin.gpv.gui.gauge

import java.awt.AlphaComposite
import java.awt.image.BufferedImage

import peregin.gpv.model.Telemetry


trait DashboardPainter {
  val speedGauge = new RadialSpeedGauge {}
  val cadenceGauge = new CadenceGauge {}
  val elevationGauge = new IconicElevationGauge {}
  val distanceGauge = new IconicDistanceGauge {}
  val heartRateGauge = new IconicHeartRateGauge {}

  def paintGauges(telemetry: Telemetry, tsInMillis: Long, image: BufferedImage, shiftInMillis: Long) {

    val g = image.createGraphics

    // set transparency
    g.setComposite(AlphaComposite.SrcOver.derive(0.5f))

    telemetry.sonda(tsInMillis + shiftInMillis).foreach{sonda =>
      val stash = g.getTransform

      speedGauge.paint(g, 75, 75, sonda)
      if (sonda.cadence.isDefined) {
        g.translate(75, 0)
        cadenceGauge.paint(g, 75, 75, sonda)
      }
      g.translate(75, 0)
      elevationGauge.paint(g, 75, 75, sonda)
      g.translate(75, 0)
      distanceGauge.paint(g, 75, 75, sonda)
      if (sonda.heartRate.isDefined) {
        g.translate(75, 0)
        heartRateGauge.paint(g, 75, 75, sonda)
      }

      // restore any kind of transformations until this point
      g.setTransform(stash)
    }

    g.dispose()
  }
}
