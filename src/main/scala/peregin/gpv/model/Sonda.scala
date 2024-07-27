package peregin.gpv.model

import org.joda.time.DateTime
import org.jdesktop.swingx.mapviewer.GeoPosition

object Sonda {

  def empty: Sonda = zeroAt(new DateTime(0))

  def zeroAt(t: DateTime): Sonda = new Sonda(t, InputValue.zero,
    new GeoPosition(0, 0),
    InputValue.empty, InputValue.empty,
    InputValue.empty, InputValue.empty, InputValue.empty,
    None, None, None, None
  )

  def sample(): Sonda = new Sonda(
    time = DateTime.now(), elapsedTime = InputValue.zero,
    location = new GeoPosition(47.366074, 8.541264), // Buerkliplatz, Zurich, Switzerland
    elevation = InputValue(480, MinMax.max(640)), grade = InputValue.empty,
    distance = InputValue(4, MinMax.max(12)), speed = InputValue(32, MinMax.max(61)),
    bearing = InputValue(90, MinMax.max(360)),
    cadence = Some(InputValue(81, MinMax.max(100))),
    heartRate = Some(InputValue(110, MinMax.max(160))), power = Some(InputValue(223, MinMax.max(320))),
    temperature = Some(InputValue(30, MinMax.max(100)))
  )
}

case class Sonda(time: DateTime, elapsedTime: InputValue,
                 location: GeoPosition,
                 elevation: InputValue, grade: InputValue,
                 distance: InputValue, speed: InputValue, bearing: InputValue,
                 cadence: Option[InputValue], heartRate: Option[InputValue], power: Option[InputValue],
                 temperature: Option[InputValue]) {

  // just for debugging purposes
  private var trackIndex: Int = 0
  private var videoProgressInMillis = 0L 

  def withTrackIndex(v: Int): Sonda = {
    trackIndex = v
    this
  }
  def getTrackIndex: Int = trackIndex
  
  def videoProgress_= (progressInMillis: Long): Unit = videoProgressInMillis = progressInMillis
  def videoProgress: Long = videoProgressInMillis
}
