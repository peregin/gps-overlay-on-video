package peregin.gpv.gui

import peregin.gpv.gui.gauge._
import peregin.gpv.model.Telemetry
import peregin.gpv.util.Timed

import scala.swing.event.MouseClicked
import scala.swing.{Dialog, GridPanel}


class GaugePanel extends GridPanel(0, 5) with Timed {

  val gaugeFactories = Seq(
    () => new GaugeComponent(new RadialSpeedGauge),
    () => new GaugeComponent(new RadialAzimuthGauge),
    () => new GaugeComponent(new CadenceGauge),
    //() => new GaugeComponent(new DrawnElevationGauge,
    //() => new GaugeComponent(new DrawnDistanceGauge,
    //() => new GaugeComponent(new DrawnHeartRateGauge,
    () => new GaugeComponent(new LinearElevationGauge),
    () => new GaugeComponent(new DigitalSpeedGauge),
    () => new GaugeComponent(new DigitalElevationGauge),
    () => new GaugeComponent(new SvgElevationGauge),
    () => new GaugeComponent(new SvgDistanceGauge),
    () => new GaugeComponent(new SvgHeartRateGauge),
    () => new GaugeComponent(new SvgPowerGauge),
    () => new GaugeComponent(new ElevationChart)
  )

  private lazy val sample = Telemetry.sample()

  gaugeFactories.foreach { fac =>
    // create the component
    val comp = withTelemetryForCharts(fac())

    // react on mouse clicks
    listenTo(comp.mouse.clicks)
    reactions += {
      case MouseClicked(`comp`, pt, _, _, false) =>
        val dlg = new Dialog() {
          modal = true
          peer.setUndecorated(false)
          contents = new GaugeTestPanel(withTelemetryForCharts(fac()))
        }
        Goodies.mapEscapeTo(dlg, () => dlg.close())
        dlg.pack()
        Goodies.center(dlg)
        dlg.visible = true
    }
    contents += comp
  }

  def withTelemetryForCharts(fac: GaugeComponent) = fac match {
    case chart: ChartPainter =>
      chart.telemetry = sample
      chart.input = chart.defaultInput
      chart
    case gauge => gauge
  }
}
