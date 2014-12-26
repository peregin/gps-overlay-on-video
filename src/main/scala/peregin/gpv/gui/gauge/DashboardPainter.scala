package peregin.gpv.gui.gauge

import java.awt.AlphaComposite
import java.awt.image.BufferedImage

import peregin.gpv.model.Telemetry


trait DashboardPainter {
  val speedGauge = new RadialSpeedGauge {}
  val cadenceGauge = new CadenceGauge {}
  val elevationChart = new ElevationChart {}
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
      // shift to the bottom
      g.translate(0, image.getHeight - boxSize)

      // paint gauges and charts
      speedGauge.paint(g, boxSize, boxSize, sonda)
      if (sonda.cadence.isDefined) {
        g.translate(boxSize, 0)
        cadenceGauge.paint(g, boxSize, boxSize, sonda)
      }
      g.translate(boxSize, 0)
      elevationChart.telemetry = telemetry
      elevationChart.paint(g, boxSize * 3, boxSize, sonda)
      g.translate(boxSize * 3, 0)
      if (sonda.heartRate.isDefined) {
        heartRateGauge.paint(g, boxSize, boxSize, sonda)
      }

      // restore any kind of transformations until this point
      g.setTransform(stash)
    }

    g.dispose()
  }
}
