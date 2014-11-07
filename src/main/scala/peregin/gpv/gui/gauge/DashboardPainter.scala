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
    g.setComposite(AlphaComposite.SrcOver.derive(0.6f))

    telemetry.sonda(tsInMillis + shiftInMillis).foreach{sonda =>
      val stash = g.getTransform

      val boxSize = 90
      speedGauge.paint(g, boxSize, boxSize, sonda)
      if (sonda.cadence.isDefined) {
        g.translate(boxSize, 0)
        cadenceGauge.paint(g, boxSize, boxSize, sonda)
      }
      g.translate(boxSize, 0)
      elevationGauge.paint(g, boxSize, boxSize, sonda)
      g.translate(boxSize, 0)
      distanceGauge.paint(g, boxSize, boxSize, sonda)
      if (sonda.heartRate.isDefined) {
        g.translate(boxSize, 0)
        heartRateGauge.paint(g, boxSize, boxSize, sonda)
      }

      // restore any kind of transformations until this point
      g.setTransform(stash)
    }

    g.dispose()
  }
}
