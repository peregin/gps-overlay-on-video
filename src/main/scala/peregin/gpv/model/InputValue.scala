package peregin.gpv.model

object InputValue {

  def empty = new InputValue(0d, MinMax.empty)
  def zero = new InputValue(0d, MinMax.zero)
}

case class InputValue(current: Double, boundary: MinMax) {

  def isInTop(percent: Int) = (current - boundary.min) * 100 / boundary.diff > 100 - percent
}
