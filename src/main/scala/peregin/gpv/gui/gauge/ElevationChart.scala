package peregin.gpv.gui.gauge

import java.awt.{Font, AlphaComposite, Color}

import peregin.gpv.gui.map.KnobPainter
import peregin.gpv.model._
import peregin.gpv.util.UnitConverter

import scala.swing.Graphics2D


trait ElevationChart extends ChartPainter with KnobPainter {

  protected var mode: Mode = Mode.DistanceBased
  protected var poi: Option[Sonda] = None
  protected var progress: Option[Sonda] = None

  protected val elevFont = new Font("Arial", Font.BOLD, 10)
  protected var metersWidth = 0
  protected var metersHalfHeight = 0
  protected var timeWidth = 0

  // shows the current elevation and grade on the middle of the chart
  protected var showCurrentValuesOnChart = true
  protected var showGrid = false

  def elevationMode = mode

  // extract the data needed
  override def sample(sonda: Sonda) {
    progress = Some(sonda)
  }

  // for the test mode
  override def input_=(v: InputValue) {
    super.input_=(v)
    progress = telemetry.distanceForProgress(v.current).map(telemetry.sondaForDistance)
  }

  // default value to be shown
  override def defaultInput = InputValue(30, MinMax(0, 100))

  override def paint(g: Graphics2D, width: Int, height: Int) {
    super.paint(g, width, height)

    val elevFm = g.getFontMetrics(elevFont)
    metersWidth = elevFm.stringWidth("3000 m")
    metersHalfHeight = elevFm.getAscent / 2
    timeWidth = elevFm.stringWidth("00:00:00")

    // some predefined values for painting
    g.setFont(elevFont)
    // this is used in the point2Time function as well
    val gridLeft = 10 + metersWidth
    val gridRight = width - 10
    val gridBottom = height - 10 - elevFm.getHeight
    val pxWidth = gridRight - gridLeft
    val pxHeight = height - 20 - elevFm.getHeight

    // coordinates, only if the track is not empty
    data.filter(_.track.nonEmpty).foreach{ telemetry =>
      val mHeight = telemetry.elevationBoundary.diff

      // legend
      g.setColor(Color.black)
      g.drawString(f"${UnitConverter.elevation(telemetry.elevationBoundary.max, units)}%1.0f ${UnitConverter.elevationUnits(units)}%s", 10, 10 + metersHalfHeight)
      g.drawString(f"${UnitConverter.elevation(telemetry.elevationBoundary.min, units)}%1.0f ${UnitConverter.elevationUnits(units)}%s", 10, height - 10 - elevFm.getHeight + metersHalfHeight)
      val distanceTotal = f"  ${UnitConverter.distance(telemetry.totalDistance, units)}%1.1f${UnitConverter.distanceUnits(units)}%s"
      val distanceWidth = elevFm.stringWidth(distanceTotal)
      val timeFirst = telemetry.minTime.toString("HH:mm:ss")
      val timeLast = telemetry.maxTime.toString("HH:mm:ss")
      g.drawString(timeFirst, gridLeft, height - 10 + metersHalfHeight)
      g.drawString(timeLast, gridRight - timeWidth - distanceWidth, height - 10 + metersHalfHeight)

      // elevation map
      val compositeStash = g.getComposite
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
      g.setComposite(compositeStash)
    }

    // grid
    if (showGrid) {
      g.setColor(Color.gray)
      for (y <- 10 until height - 10 by math.max(1, pxHeight / 6)) {
        g.drawLine(gridLeft, y, gridRight, y)
      }
      for (x <- gridLeft until gridRight by math.max(1, pxWidth / 8)) {
        g.drawLine(x, 10, x, gridBottom)
      }
    } else {
      // just the top line
      val y = 10
      g.drawLine(gridLeft, y, gridRight, y)
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
      g.drawString(f"${UnitConverter.distance(sonda.distance.current, units)}%1.1f${UnitConverter.distanceUnits(units)}%s", gridLeft + (timeWidth * 2.9).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${UnitConverter.speed(sonda.speed.current, units)}%1.1f${UnitConverter.speedUnits(units)}%s", gridLeft + (timeWidth * 3.9).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${UnitConverter.elevation(sonda.elevation.current, units)}%1.0f${UnitConverter.elevationUnits(units)}%s", gridLeft + (timeWidth * 5.3).toInt, height - 10 + metersHalfHeight)
      g.drawString(f"${sonda.grade.current}%1.2f%%", gridLeft + (timeWidth * 6.2).toInt, height - 10 + metersHalfHeight)
    }

    // progress when playing the video
    progress.foreach{sonda =>
      // position
      val p = mode match {
        case Mode.TimeBased => telemetry.progressForTime(sonda.time)
        case Mode.DistanceBased => telemetry.progressForDistance(sonda.distance.current)
      }
      val x = (gridLeft + p * pxWidth / 100).toInt
      g.setColor(Color.orange)
      g.drawLine(x, 10, x, gridBottom)
      paintKnob(g, x, 10, Color.orange)

      if (showCurrentValuesOnChart) {
        // altitude
        val alt = sonda.elevation.current
        g.setFont(gaugeFont.deriveFont(Font.BOLD, (pxHeight / 8).toFloat))
        val elevationText = f"${UnitConverter.elevation(alt, units)}%2.0f ${UnitConverter.elevationUnits(units)}%s"
        val atb = g.getFontMetrics.getStringBounds(elevationText, g)
        val middleHeight = gridBottom - (height - atb.getHeight) / 2
        textWidthShadow(g, elevationText, gridLeft + (pxWidth - atb.getWidth) / 2, middleHeight)
        // slope grade
        val slope = sonda.grade.current
        val slopeText = f"$slope%2.0f %%"
        val stb = g.getFontMetrics.getStringBounds(slopeText, g)
        textWidthShadow(g, slopeText, gridLeft + (pxWidth - stb.getWidth) / 2, middleHeight + stb.getHeight)
        // total distance
        val distanceTotal = f"${UnitConverter.distance(telemetry.totalDistance, units)}%1.1f${UnitConverter.distanceUnits(units)}%s"
        val dtb = g.getFontMetrics.getStringBounds(distanceTotal, g)
        textWidthShadow(g, distanceTotal, gridRight - dtb.getWidth, middleHeight)
      }
    }
  }
}
