package peregin.gpv.gui.dashboard

import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import peregin.gpv.gui.gauge._
import peregin.gpv.model.Telemetry

import java.awt.geom.AffineTransform

trait DashboardPainter {

  // default dashboard painter
  @volatile private var dashboard: Dashboard = new CyclingDashboard {}

  def dash: Dashboard = dashboard
  def dash_= (d: Dashboard): Unit = dashboard = d

  def paintGauges(telemetry: Telemetry, tsInMillis: Long, image: BufferedImage, rotation: Double, shiftInMillis: Long, transparencyInPercentage: Double, units: String): Unit = {

    val g = image.createGraphics

    // set transparency
    val alpha = (transparencyInPercentage / 100).min(1d).max(0d)
    g.setComposite(AlphaComposite.SrcOver.derive(alpha.toFloat))

    // set units and telemetry
    dashboard.gauges().foreach{ gp =>
      gp.units = units
      gp match {
        case painter: ChartPainter =>
          painter.transparency = transparencyInPercentage
          painter.telemetry = telemetry
        case _ =>
      }
    }

    telemetry.sondaForRelativeTime(tsInMillis + shiftInMillis).foreach{ sonda =>
      val stash = g.getTransform

      // adjusted to the size of the image proportionally
      val width = if (rotation == 0 || Math.abs(rotation) == 180) image.getWidth else image.getHeight
      val height = if (rotation == 0 || Math.abs(rotation) == 180) image.getHeight else image.getWidth
      val f = height match {
        case p360 if p360 <= 360 => 4
        case _ => 5
      }
      val boxSize = (width min height) / f

      val at = new AffineTransform()
      // And we rotate about the center of the image...
      val x = width / 2
      val y = height / 2
      at.translate((height - image.getHeight) / 2, (width - image.getWidth) / 2)
      at.rotate(Math.toRadians(rotation), x, y)
      g.transform(at)

      // paint dashboard
      dashboard.paintDashboard(g, width, height, boxSize, sonda)

      // restore any kind of transformations until this point
      g.setTransform(stash)
    }

    g.dispose()
  }
}
