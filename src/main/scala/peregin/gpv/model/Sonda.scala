package peregin.gpv.model

import org.joda.time.DateTime
import org.jdesktop.swingx.mapviewer.GeoPosition

object Sonda {

  def zeroAt(t: DateTime) = new Sonda(t, new GeoPosition(0, 0),
    InputValue.zero, InputValue.zero,
    InputValue.zero, InputValue.zero,
    None, None
  )
}

case class Sonda(time: DateTime, location: GeoPosition,
                 elevation: InputValue, grade: InputValue,
                 distance: InputValue, speed: InputValue,
                 cadence: Option[InputValue], heartRate: Option[InputValue]) {

  // just for debugging purposes
  private var trackIndex: Int = 0

  def withTrackIndex(v: Int): Sonda = {
    trackIndex = v
    this
  }

  def getTrackIndex = trackIndex
}
