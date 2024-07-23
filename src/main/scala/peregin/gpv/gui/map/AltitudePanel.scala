package peregin.gpv.gui.map

import java.awt.Color

import peregin.gpv.gui.gauge.ElevationChart
import peregin.gpv.model.{Mode, Sonda, Telemetry}
import peregin.gpv.util.Logging

import scala.swing._

// altitude widget
class AltitudePanel extends Panel with Logging {

  val elevationChart: ElevationChart = new ElevationChart

  elevationChart.showCurrentValuesOnChart = false
  elevationChart.showGrid = true

  override def paint(g: Graphics2D) = {
    val width = peer.getWidth
    val height = peer.getHeight

    // background
    g.setColor(Color.white)
    g.fillRect(0, 0, width, height)

    elevationChart.paint(g, width, height)
  }

  def sondaForPoint(pt: Point): Option[Sonda] = {
    val p = progressForPoint(pt)
    val sonda = elevationChart.elevationMode match {
      case Mode.TimeBased => elevationChart.telemetry.timeForProgress(p).map(elevationChart.telemetry.sondaForAbsoluteTime)
      case Mode.DistanceBased => elevationChart.telemetry.distanceForProgress(p).map(elevationChart.telemetry.sondaForDistance)
    }
    log.info(s"track index = ${sonda.map(_.getTrackIndex).getOrElse(0)}")
    sonda
  }

  private def progressForPoint(pt: Point): Double = {
    val x = pt.x
    val width = peer.getWidth
    // constants below are defined in the paint method as well
    val gridLeft = 10 + elevationChart.metersWidth
    val gridRight = width - 10
    (x - gridLeft).toDouble * 100 / (gridRight - gridLeft)
  }

  def refresh(telemetry: Telemetry): Unit = {
    elevationChart.telemetry = telemetry
  }

  def refresh(mode: Mode): Unit = {
    elevationChart.mode = mode
    repaint()
  }

  def refreshPoi(sonda: Option[Sonda]): Unit = {
    elevationChart.poi = sonda
    repaint()
  }

  def refreshProgress(sonda: Option[Sonda]): Unit = {
    elevationChart.progress = sonda
    repaint()
  }
}
