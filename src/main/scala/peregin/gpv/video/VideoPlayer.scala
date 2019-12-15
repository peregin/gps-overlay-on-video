package peregin.gpv.video

import java.awt.image.BufferedImage


// extract the expected functionality from the player into this interface
// implementation can be easily replaced (media vs tool based implementation form xuggler or something completely different)
object VideoPlayer {
  trait Listener {

    def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage): Unit
    def seekEvent(percentage: Double): Unit

    def videoStopped(): Unit
    def videoStarted(): Unit
  }
}

trait VideoPlayer {
  def play(): Unit
  def step(): Unit
  def pause(): Unit
  def seek(percentage: Double): Unit
  def close(): Unit
  def duration: Long // retrieves the duration of the video stream in millis
  def playing: Boolean // tells whether the player is on or not
}

trait VideoPlayerFactory {
  def createPlayer(url: String, listener: VideoPlayer.Listener): VideoPlayer
}
