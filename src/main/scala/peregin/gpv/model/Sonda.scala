package peregin.gpv.model

import org.joda.time.DateTime


case class Sonda(time: DateTime,
                 elevation: InputValue) {

  // just for debugging purposes
  private var trackIndex: Int = 0

  def withTrackIndex(v: Int): Sonda = {
    trackIndex = v
    this
  }
  def getTrackIndex = trackIndex
}
