package peregin.gpv.model

import MinMax._

object MinMax {
  def zero = new MinMax(0, 0)
  def extreme = new MinMax(Double.MaxValue, Double.MinValue)

  implicit class RoundedDouble(v: Double) {
    def roundUpToTenth: Double = roundUpTo(10)
    def roundUpToHundredth: Double = roundUpTo(100)
    private def roundUpTo(base: Int): Double = {
      val r = v % base
      val adjust = if (r <= 0) 0d else base.toDouble
      v - r + adjust
    }

    def roundDownToTenth: Double = roundDownTo(10)
    def roundDownToHundredth: Double = roundDownTo(100)
    def roundDownTo(base: Int): Double = {
      val r = v % base
      val adjust = if (r >= 0) 0d else base.toDouble
      v - r - adjust
    }
  }
}

case class MinMax(var min: Double, var max: Double) {

  // range of [min, max) - min inclusive, max exclusive
  def includes(v: Double): Boolean = v >= min && v < max

  def sample(sample: Double): Unit = {
    if (sample < min) min = sample
    else if (sample > max) max = sample
  }

  def mean: Double = (max + min) / 2

  def diff: Double = max - min

  def tenths: Int = (max.roundUpToTenth - min.roundDownToTenth).toInt
  def hundredths: Int = (max.roundUpToHundredth - min.roundDownToHundredth).toInt

  override def toString: String = f"${getClass.getSimpleName}($min%.01f,$max%.01f)"
}
