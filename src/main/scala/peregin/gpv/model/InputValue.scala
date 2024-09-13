package peregin.gpv.model

object InputValue {

  def empty = new InputValue(None, MinMax.empty)
  def zero = new InputValue(Some(0d), MinMax.zero)
}

case class InputValue(current: Option[Double], boundary: MinMax) {

  def isInTop(percent: Int) = (current.getOrElse(boundary.min) - boundary.min) * 100 / boundary.diff > 100 - percent
}
