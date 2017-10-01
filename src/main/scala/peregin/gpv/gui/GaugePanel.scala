package peregin.gpv.gui

import peregin.gpv.gui.gauge._
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Io, Timed}

import scala.swing.event.MouseClicked
import scala.swing.{Dialog, GridPanel}
import scala.util.Try


class GaugePanel extends GridPanel(0, 5) with Timed {

  val gaugeFactories = Seq(
    () => new GaugeComponent with RadialSpeedGauge,
    () => new GaugeComponent with CadenceGauge,
    //() => new GaugeComponent with DrawnElevationGauge,
    //() => new GaugeComponent with DrawnDistanceGauge,
    //() => new GaugeComponent with DrawnHeartRateGauge,
    () => new GaugeComponent with LinearElevationGauge,
    () => new GaugeComponent with DigitalSpeedGauge,
    () => new GaugeComponent with DigitalElevationGauge,
    () => new GaugeComponent with SvgElevationGauge,
    () => new GaugeComponent with SvgDistanceGauge,
    () => new GaugeComponent with SvgHeartRateGauge,
    () => new GaugeComponent with SvgPowerGauge,
    () => new GaugeComponent with ElevationChart
  )

  lazy val sample = timed("load sample gps data") {
    Try(Telemetry.load(Io.getResource("gps/sample.gpx"))).toOption.getOrElse(Telemetry.empty)
  }

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

  def withTelemetryForCharts(fac: GaugeComponent with GaugePainter) = fac match {
    case chart: ChartPainter =>
      chart.telemetry = sample
      chart.input = chart.defaultInput
      chart
    case gauge => gauge
  }
}
