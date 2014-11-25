package peregin.gpv.model

import java.awt.Color

sealed abstract class SlopeSegment(grade: MinMax, val color: Color) {

  def includes(slope: Double) = grade.includes(slope)

  override def toString = f"${getClass.getSimpleName.dropRight(1)} (${grade.min}%.0f - ${grade.max}%.0f)%%"
}

object SlopeSegment {
  case object Easy extends SlopeSegment(MinMax(0, 4), Color.green)
  case object Rolling extends SlopeSegment(MinMax(4, 7), Color.yellow)
  case object Worker extends SlopeSegment(MinMax(7, 10), Color.orange)
  case object Hard extends SlopeSegment(MinMax(10, 15), Color.red)
  case object Painful extends SlopeSegment(MinMax(15, Double.MaxValue), new Color(100, 0, 0))

  // slope is expected in percentage
  def identify(slope: Double) = slope.abs match {
    case v if Easy.includes(v) => Easy
    case v if Rolling.includes(v) => Rolling
    case v if Worker.includes(v) => Worker
    case v if Hard.includes(v) => Hard
    case v if Painful.includes(v) => Painful
  }
}
