package peregin.gpv.gui.map

import java.awt.{AlphaComposite, Color, Font}

import org.joda.time.DateTime
import peregin.gpv.model.{SlopeSegment, Telemetry, Sonda}

import scala.swing._

// altitude widget
class AltitudePanel extends Panel with KnobPainter {

  sealed trait Mode
  object Mode {
    case object TimeBased extends Mode
    case object DistanceBased extends Mode
  }

  private var telemetry = Telemetry.empty
  private var poi: Option[Sonda] = None
  private var progress: Option[Sonda] = None
  private var mode: Mode = Mode.DistanceBased

  val elevFont = new Font("Arial", Font.BOLD, 10)
  lazy val elevFm = peer.getGraphics.getFontMetrics(elevFont)
  lazy val metersWidth = elevFm.stringWidth("3000 m")
  lazy val metersHalfHeight = elevFm.getAscent / 2
  lazy val timeWidth = elevFm.stringWidth("00:00:00")

  def elevationMode = mode

  override def paint(g: Graphics2D) = {
    val width = peer.getWidth
    val height = peer.getHeight

    // background
    g.setColor(Color.white)
    g.fillRect(0, 0, width, height)

    // some predefined values for painting
    g.setFont(elevFont)
    // this is used in the point2Time function as well
    val gridLeft = 10 + metersWidth
    val gridRight = width - 10
    val gridBottom = height - 10 - elevFm.getHeight
    val pxWidth = gridRight - gridLeft
    val pxHeight = height - 20 - elevFm.getHeight

    // coordinates, only if the track is not empty
    if (telemetry.track.nonEmpty) {
      val mHeight = telemetry.elevationBoundary.diff

      // legend
      g.setColor(Color.black)
      g.drawString(s"${telemetry.elevationBoundary.max.toInt} m", 10, 10 + metersHalfHeight)
      g.drawString(s"${telemetry.elevationBoundary.min.toInt} m", 10, height - 10 - elevFm.getHeight + metersHalfHeight)
      val timeFirst = telemetry.minTime.toString("HH:mm:ss")
      val timeLast = telemetry.maxTime.toString("HH:mm:ss")
      g.drawString(timeFirst, gridLeft, height - 10 + metersHalfHeight)
      g.drawString(timeLast, gridRight - timeWidth, height - 10 + metersHalfHeight)

      // max distance and speed
      g.setColor(Color.red)
      g.drawString(f"${telemetry.speedBoundary.max}%1.1f", 10, 10 + metersHalfHeight + elevFm.getHeight)
      g.drawString("km/h", 10, 10 + metersHalfHeight + 2 * elevFm.getHeight)
      g.drawString(f"${telemetry.totalDistance}%1.1f", 10, 10 + metersHalfHeight + 4 * elevFm.getHeight)
      g.drawString("km", 10, 10 + metersHalfHeight + 5 * elevFm.getHeight)

      // elevation map
      g.setComposite(AlphaComposite.SrcOver.derive(0.3f)) // transparent because of the different slope segment colors
      for (i <- 0 until pxWidth) {
        val f = i.toDouble * 100 / pxWidth // use double value for the percentage
        val sondaCandidate = mode match {
          case Mode.DistanceBased => telemetry.distanceForProgress(f).map(telemetry.sondaForDistance)
          case Mode.TimeBased => telemetry.timeForProgress(f).map(telemetry.sondaForAbsoluteTime)
        }
        sondaCandidate.foreach{ sonda =>
          val v = sonda.elevation.current - telemetry.elevationBoundary.min
          val x = gridLeft + i
          val y = v * pxHeight / mHeight
          //log.info(s"x=$i f=$f elev=${sonda.elevation.current} y=$y")
          val slope = SlopeSegment.identify(sonda.grade.current)
          g.setColor(slope.color)
          g.drawLine(x, gridBottom, x, gridBottom - y.toInt)
          g.setColor(Color.black)
          g.drawLine(x, gridBottom - y.toInt, x, gridBottom - y.toInt - 1)
        }
      }
      g.setComposite(AlphaComposite.SrcOver.derive(1f))
    }

    // grid
    g.setColor(Color.gray)
    for (y <- 10 until height - 10 by math.max(1, pxHeight / 6)) {
      g.drawLine(gridLeft, y, gridRight, y)
    }
    for (x <- gridLeft until gridRight by math.max(1, pxWidth / 8)) {
      g.drawLine(x, 10, x, gridBottom)
    }

    // POI marker and data
    poi.foreach{sonda =>
      val p = mode match {
        case Mode.TimeBased => telemetry.progressForTime(sonda.time)
        case Mode.DistanceBased => telemetry.progressForDistance(sonda.distance.current)
      }
      val x = (gridLeft + p * pxWidth / 100).toInt
      g.setColor(Color.blue)
      g.drawLine(x, 10, x, gridBottom)
      paintKnob(g, x, 10, Color.blue)

      // draw data; time, speed, distance
      g.setColor(Color.blue)
      g.drawString(sonda.time.toString("HH:mm:ss"), gridLeft + (timeWidth * 1.5).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${sonda.elevation.current}%1.0fm", gridLeft + (timeWidth * 2.9).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${sonda.distance.current}%1.1fkm", gridLeft + (timeWidth * 3.9).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${sonda.speed.current}%1.1fkm/h", gridLeft + (timeWidth * 4.9).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${sonda.grade.current}%1.2f%%", gridLeft + (timeWidth * 6.1).toInt, height - 10 + metersHalfHeight)
    }

    // progress when playing the video
    progress.foreach{sonda =>
      val p = mode match {
        case Mode.TimeBased => telemetry.progressForTime(sonda.time)
        case Mode.DistanceBased => telemetry.progressForDistance(sonda.distance.current)
      }
      val x = (gridLeft + p * pxWidth / 100).toInt
      g.setColor(Color.orange)
      g.drawLine(x, 10, x, gridBottom)
      paintKnob(g, x, 10, Color.orange)
    }
  }

  def progressForPoint(pt: Point): Double = {
    val x = pt.x
    val width = peer.getWidth
    // constants below are defined in the paint method as well
    val gridLeft = 10 + metersWidth
    val gridRight = width - 10
    (x - gridLeft).toDouble * 100 / (gridRight - gridLeft)
  }

  def refresh(telemetry: Telemetry) {
    this.telemetry = telemetry
  }

  def refresh(mode: Mode) {
    this.mode = mode
    repaint()
  }

  def refreshPoi(sonda: Option[Sonda]) {
    poi = sonda
    repaint()
  }

  def refreshProgress(sonda: Option[Sonda]) {
    progress = sonda
    repaint()
  }
}
