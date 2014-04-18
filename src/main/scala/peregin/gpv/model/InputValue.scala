package peregin.gpv.model


case class InputValue(current: Double, boundary: MinMax) {

  def isInTop(percent: Int) = (current - boundary.min) * 100 / boundary.diff > 100 - percent
}
