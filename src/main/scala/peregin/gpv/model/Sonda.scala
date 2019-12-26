package peregin.gpv.model

import org.joda.time.DateTime
import org.jdesktop.swingx.mapviewer.GeoPosition

object Sonda {

  def empty: Sonda = zeroAt(new DateTime(0))

  def zeroAt(t: DateTime): Sonda = new Sonda(t, InputValue.zero,
    new GeoPosition(0, 0),
    InputValue.zero, InputValue.zero,
    InputValue.zero, InputValue.zero,
    None, None, None
  )

  def sample: Sonda = new Sonda(
    time = DateTime.now(), elapsedTime = InputValue.zero,
    location = new GeoPosition(47.366074, 8.541264), // Buerkliplatz, Zurich, Switzerland
    elevation = InputValue(480, MinMax.extreme), grade = InputValue.zero,
    distance = InputValue(5000, MinMax.extreme), speed = InputValue(32, MinMax.extreme),
    cadence = Some(InputValue(81, MinMax.extreme)),
    heartRate = Some(InputValue(110, MinMax.extreme)), power = Some(InputValue(223, MinMax.extreme))
  )
}

case class Sonda(time: DateTime, elapsedTime: InputValue,
                 location: GeoPosition,
                 elevation: InputValue, grade: InputValue,
                 distance: InputValue, speed: InputValue,
                 cadence: Option[InputValue], heartRate: Option[InputValue], power: Option[InputValue]) {

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
