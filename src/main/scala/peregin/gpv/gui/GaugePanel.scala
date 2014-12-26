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
    () => new GaugeComponent with IconicElevationGauge,
    () => new GaugeComponent with IconicDistanceGauge,
    () => new GaugeComponent with LinearElevationGauge,
    () => new GaugeComponent with IconicHeartRateGauge,
    () => new GaugeComponent with DigitalSpeedGauge,
    () => new GaugeComponent with DigitalElevationGauge
  )

  val sample = timed("load sample gps data") {
    Try(Telemetry.load(Io.getResource("gps/sample.gpx"))).toOption.getOrElse(Telemetry.empty)
  }

  gaugeFactories.foreach{ fac =>
    // create the component
    val comp = fac()

    // initialize with default telemetry if needed
    if (comp.isInstanceOf[ChartPainter]) comp.asInstanceOf[ChartPainter].telemetry = sample

    // react on mouse clicks
    listenTo(comp.mouse.clicks)
    reactions += {
      case MouseClicked(`comp`, pt, _, _, false) =>
        val dlg = new Dialog() {
          modal = true
          peer.setUndecorated(false)
          contents = new GaugeTestPanel(fac())
        }
        Goodies.mapEscapeTo(dlg, () => dlg.close())
        dlg.pack()
        Goodies.center(dlg)
        dlg.visible = true
    }
    contents += comp
  }
}
