package peregin.gpv.model

object MinMax {
  def zero = new MinMax(0, 0)
  def extreme = new MinMax(Double.MaxValue, Double.MinValue)

  implicit class RoundedDouble(v: Double) {
    def roundUpToTenth = roundUpTo(10)
    def roundUpToHundredth = roundUpTo(100)
    private def roundUpTo(base: Int) = {
      val r = v % base
      val adjust = if (r <= 0) 0d else base
      v - r + adjust
    }

    def roundDownToTenth = roundDownTo(10)
    def roundDownToHundredth = roundDownTo(100)
    def roundDownTo(base: Int) = {
      val r = v % base
      val adjust = if (r >= 0) 0d else base
      v - r - adjust
    }
  }
}

case class MinMax(var min: Double, var max: Double) {

  // range of [min, max) - min inclusive, max exlusive
  def includes(v: Double) = v >= min && v < max

  def sample(sample: Double) {
    if (sample < min) min = sample
    else if (sample > max) max = sample
  }

  def mean = (max + min) / 2

  def diff = max - min

  import MinMax._
  def tenths = (max.roundUpToTenth - min.roundDownToTenth).toInt
  def hundredths = (max.roundUpToHundredth - min.roundDownToHundredth).toInt
}
