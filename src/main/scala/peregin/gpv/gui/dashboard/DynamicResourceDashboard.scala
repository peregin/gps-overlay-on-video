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
      gauge.gauge.paint(g, (imageHeight * gauge.width).toInt, (imageHeight * gauge.height).toInt, sonda)
      g.setTransform(saved)
    })
  }

  override def gauges(): Seq[GaugePainter] = {
    return definedGauges.map(setup => setup.gauge).toSeq;
  }
}
