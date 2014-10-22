package peregin.gpv.gui.video

import java.awt.Image

import peregin.gpv.model.Telemetry


// extract the expected functionality from the player into this interface
// implementation can be easily replaced (media vs tool based from xuggler)
trait VideoPlayer {

  def seek(percentage: Double)

  def close()
}

trait VideoPlayerFactory {
  def createPlayer(url: String, telemetry: Telemetry, imageHandler: Image => Unit,
             shiftHandler: => Long, timeUpdater: (Long, Int) => Unit): VideoPlayer
}
