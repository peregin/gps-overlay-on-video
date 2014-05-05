package peregin.gpv.util

import scala.math._


object Trigo {

  def square(v: Double) = v * v

  def pythagoras(a: Double, b: Double): Double = sqrt(square(a) + square(b))
  def leg(c: Double, a: Double): Double = sqrt(square(c) - square(a))

  def polarX(cx: Double, r: Double, angle: Double): Int = (cx + r * math.cos(math.toRadians(angle))).toInt
  def polarY(cy: Double, r: Double, angle: Double): Int = (cy + r * math.sin(math.toRadians(angle))).toInt
}
