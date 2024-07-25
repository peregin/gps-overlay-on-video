package peregin.gpv.gui.dashboard

import peregin.gpv.gui.gauge.GaugePainter


case class GaugeSetup(x: Double, y: Double, size: Option[Double], width: Option[Double], height: Option[Double], gauge: GaugePainter) {
  if (width.isEmpty && size.isEmpty) {
    throw new IllegalArgumentException("Either width or size must be defined")
  }
  if (height.isEmpty && size.isEmpty) {
    throw new IllegalArgumentException("Either height or size must be defined")
  }
}
