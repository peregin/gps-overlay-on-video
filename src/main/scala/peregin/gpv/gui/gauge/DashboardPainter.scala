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

  def paintGauges(telemetry: Telemetry, tsInMillis: Long, image: BufferedImage, shiftInMillis: Long, transparencyInPercentage: Double) {

    val g = image.createGraphics

    // set transparency
    val alpha = (transparencyInPercentage / 100).min(1d).max(0d)
    g.setComposite(AlphaComposite.SrcOver.derive(alpha.toFloat))

    telemetry.sondaForRelativeTime(tsInMillis + shiftInMillis).foreach{sonda =>
      val stash = g.getTransform

      // adjusted to the size of the image proportionally
      val boxSize = image.getWidth.min(image.getHeight) / 5
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
