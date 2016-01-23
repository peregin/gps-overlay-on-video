package peregin.gpv.gui.gauge

import java.awt.AlphaComposite
import java.awt.image.BufferedImage

import peregin.gpv.model.Telemetry


trait DashboardPainter {
  val speedGauge = new RadialSpeedGauge {}
  val cadenceGauge = new CadenceGauge {}
  val elevationChart = new ElevationChart {}
  val heartRateGauge = new IconicHeartRateGauge {}

  def paintGauges(telemetry: Telemetry, tsInMillis: Long, image: BufferedImage, shiftInMillis: Long, transparencyInPercentage: Double, units: String) {

    val g = image.createGraphics

    // set transparency
    val alpha = (transparencyInPercentage / 100).min(1d).max(0d)
    g.setComposite(AlphaComposite.SrcOver.derive(alpha.toFloat))

    // set units
    speedGauge.units = units
    elevationChart.units = units

    telemetry.sondaForRelativeTime(tsInMillis + shiftInMillis).foreach{sonda =>
      val stash = g.getTransform

      // adjusted to the size of the image proportionally
      val width = image.getWidth
      val height = image.getHeight
      val f = height match {
        case p360 if p360 <= 360 => 4
        case _ => 5
      }
      val boxSize = (width min height) / f
      // shift to the bottom
      g.translate(0, height - boxSize)


      // paint elevation to the right
      val stashBottom = g.getTransform
      g.translate(width - boxSize * 3, 0)
      elevationChart.telemetry = telemetry
      elevationChart.paint(g, boxSize * 3, boxSize, sonda)
      g.setTransform(stashBottom)

      // paint gauges and charts
      speedGauge.paint(g, boxSize, boxSize, sonda)
      g.translate(boxSize, 0)
      if (sonda.cadence.isDefined) {
        cadenceGauge.paint(g, boxSize, boxSize, sonda)
        g.translate(boxSize, 0)
      }
      if (sonda.heartRate.isDefined) {
        heartRateGauge.paint(g, boxSize, boxSize, sonda)
        g.translate(boxSize, 0)
      }

      // restore any kind of transformations until this point
      g.setTransform(stash)
    }

    g.dispose()
  }
}
