package peregin.gpv.model

import org.joda.time.DateTime
import org.jdesktop.swingx.mapviewer.GeoPosition

object Sonda {

  def zeroAt(t: DateTime) = new Sonda(t, InputValue.zero,
    new GeoPosition(0, 0),
    InputValue.zero, InputValue.zero,
    InputValue.zero, InputValue.zero,
    None, None
  )
}

case class Sonda(time: DateTime, elapsedTime: InputValue,
                 location: GeoPosition,
                 elevation: InputValue, grade: InputValue,
                 distance: InputValue, speed: InputValue,
                 cadence: Option[InputValue], heartRate: Option[InputValue]) {

  // just for debugging purposes
  private var trackIndex: Int = 0
  private var videoProgressInMillis = 0L 

  def withTrackIndex(v: Int): Sonda = {
    trackIndex = v
    this
  }
  def getTrackIndex = trackIndex
  
  def videoProgress_= (progressInMillis: Long) = videoProgressInMillis = progressInMillis
  def videoProgress = videoProgressInMillis
}
