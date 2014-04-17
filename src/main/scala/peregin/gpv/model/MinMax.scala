package peregin.gpv.model

object MinMax {
  def zero = new MinMax(0, 0)
  def extreme = new MinMax(Double.MaxValue, Double.MinValue)

  implicit class RoundedDouble(v: Double) {
    def roundUpToTenth = {
      val r = v % 10
      val adjust = if (r <= 0) 0d else 10d
      v - r + adjust
    }
    def roundDownToTenth = {
      val r = v % 10
      val adjust = if (r >= 0) 0d else 10d
      v - r - adjust
    }
  }
}

case class MinMax(var min: Double, var max: Double) {

  def sample(sample: Double) {
    if (sample < min) min = sample
    else if (sample > max) max = sample
  }

  def mean = (max + min) / 2

  def diff = max - min
}
