package peregin.gpv.gui.dashboard

import peregin.gpv.gui.gauge.GaugePainter
import peregin.gpv.model.Sonda

import java.awt.geom.AffineTransform
import scala.swing.Graphics2D


class DynamicResourceDashboard(gauges: Seq[GaugeSetup]) extends Dashboard {
  private val definedGauges: Array[GaugeSetup] = gauges.toArray

  override def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    definedGauges.foreach(gauge => {
      val saved = g.getTransform
      g.setTransform(new AffineTransform())
      g.translate((gauge.x * imageWidth).toInt, (gauge.y * imageHeight).toInt)
      val width = (if (gauge.width.isDefined) imageWidth * gauge.width.get else imageHeight * gauge.size.get).toInt
      val height = (if (gauge.height.isDefined) imageHeight * gauge.height.get else imageHeight * gauge.size.get).toInt
      gauge.gauge.paint(g, imageHeight, width, height, sonda)
      g.setTransform(saved)
    })
  }

  override def gauges(): Seq[GaugePainter] = {
    return definedGauges.map(setup => setup.gauge).toSeq;
  }
}
