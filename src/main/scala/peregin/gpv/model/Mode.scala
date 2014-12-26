package peregin.gpv.model


sealed trait Mode
object Mode {
  case object TimeBased extends Mode
  case object DistanceBased extends Mode
}
