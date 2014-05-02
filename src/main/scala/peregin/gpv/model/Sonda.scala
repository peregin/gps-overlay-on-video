package peregin.gpv.model

import org.joda.time.DateTime

object Sonda {

  def zeroAt(t: DateTime) = new Sonda(t,
    InputValue.zero, InputValue.zero,
    InputValue.zero, InputValue.zero)
}

case class Sonda(time: DateTime,
                 elevation: InputValue, grade: InputValue,
                 distance: InputValue, speed: InputValue) {

  // just for debugging purposes
  private var trackIndex: Int = 0

  def withTrackIndex(v: Int): Sonda = {
    trackIndex = v
    this
  }

  def getTrackIndex = trackIndex
}
