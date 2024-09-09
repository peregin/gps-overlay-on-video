package peregin.gpv.gui.gauge

import peregin.gpv.model.Telemetry

/**
 * In addition to the gauge painter the chart painter is initialized with the current telemetry data, thus allowing
 * to paint a chart based on a specific data (such as elevation, speed, heart rate, etc).
 */
trait ChartPainter extends GaugePainter {

  protected var data: Option[Telemetry] = None

  def telemetry: Telemetry = data.getOrElse(Telemetry.empty())
  def telemetry_= (on: Telemetry): Unit = data = Some(on)

  def transparency: Float = 1.0f
  def transparency_= (transparencyInPercentage: Double): Unit = {}
}
