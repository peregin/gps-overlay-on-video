package peregin.gpv.gui.dashboard

import java.awt.AlphaComposite
import java.awt.image.BufferedImage

import peregin.gpv.gui.gauge._
import peregin.gpv.model.Telemetry

trait DashboardPainter {

  private val dashboard = new CyclingDashboard {}

  def paintGauges(telemetry: Telemetry, tsInMillis: Long, image: BufferedImage, shiftInMillis: Long, transparencyInPercentage: Double, units: String): Unit = {

    val g = image.createGraphics

    // set transparency
    val alpha = (transparencyInPercentage / 100).min(1d).max(0d)
    g.setComposite(AlphaComposite.SrcOver.derive(alpha.toFloat))

    // set units and telemetry
    dashboard.gauges().foreach{ gp =>
      gp.units = units
      gp match {
        case painter: ChartPainter =>
          painter.telemetry = telemetry
        case _ =>
      }
    }

    telemetry.sondaForRelativeTime(tsInMillis + shiftInMillis).foreach{ sonda =>
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

      // paint dashboard
      dashboard.paintDashboard(g, width, boxSize, sonda)

      // restore any kind of transformations until this point
      g.setTransform(stash)
    }

    g.dispose()
  }
}
