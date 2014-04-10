package peregin.tov.model

object MinMax {
  def zero = new MinMax(0, 0)
  def extreme = new MinMax(Double.MaxValue, Double.MinValue)
}

case class MinMax(var min: Double, var max: Double) {

  def sample(sample: Double) {
    if (sample < min) min = sample
    else if (sample > max) max = sample
  }

  def middle = (max + min) / 2
}
