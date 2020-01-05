package peregin.gpv.gui.dashboard

import peregin.gpv.gui.gauge.{CadenceGauge, ElevationChart, GaugePainter, RadialAzimuthGauge, RadialSpeedGauge, SvgHeartRateGauge, SvgPowerGauge}
import peregin.gpv.model.{InputValue, MinMax, Sonda}

import scala.swing.Graphics2D

trait Dashboard extends Cloneable {

  override def clone(): Dashboard = super.clone().asInstanceOf[Dashboard]

  def gauges(): Seq[GaugePainter]

  def paintDashboard(g: Graphics2D, imageWidth: Int, gaugeSize: Int, sonda: Sonda): Unit
}

trait CyclingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {}
  private val cadenceGauge = new CadenceGauge {}
  private val elevationChart = new ElevationChart {}
  private val heartRateGauge = new SvgHeartRateGauge {}
  private val powerGauge = new SvgPowerGauge {}

  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, cadenceGauge, elevationChart, heartRateGauge, powerGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    elevationChart.paint(g, gaugeSize * 3, gaugeSize * 3 / 4, sonda)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)
    if (sonda.cadence.isDefined) {
      cadenceGauge.paint(g, gaugeSize, gaugeSize, sonda)
      g.translate(gaugeSize, 0)
    }

    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
    if (sonda.power.isDefined) {
      if (sonda.heartRate.isDefined) g.translate(-gaugeSize2, -gaugeSize2)
      powerGauge.paint(g, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}

trait SkiingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {}
  private val elevationChart = new ElevationChart {}
  private val heartRateGauge = new SvgHeartRateGauge {}

  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, elevationChart, heartRateGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    elevationChart.paint(g, gaugeSize * 3, gaugeSize * 3 / 4, sonda)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)

    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}

trait MotorBikingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {
    override lazy val dummy: InputValue = InputValue(181, MinMax.max(230))
  }
  private val elevationChart = new ElevationChart {}
  private val heartRateGauge = new SvgHeartRateGauge {}

  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, elevationChart, heartRateGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    elevationChart.paint(g, gaugeSize * 3, gaugeSize * 3 / 4, sonda)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)

    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}

trait SailingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {}
  private val azimuthGauge = new RadialAzimuthGauge {}
  private val heartRateGauge = new SvgHeartRateGauge {}

  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, azimuthGauge, heartRateGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)
    azimuthGauge.paint(g, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)


    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}