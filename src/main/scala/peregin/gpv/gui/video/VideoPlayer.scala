package peregin.gpv.gui.video

import java.awt.Image

import peregin.gpv.model.Telemetry


// extract the expected functionality from the player into this interface
// implementation can be easily replaced (media vs tool based from xuggler)
trait VideoPlayer {

  def seek(percentage: Double)

  def close()
}

object VideoPlayer {

  type inputType = (String, Telemetry, (Image) => Unit, () => Long, (Long, Int) => Unit)

  //def create(t: inputType) = new ExperimentalVideoPlayer()

  def createSimple(url: String, telemetry: Telemetry, imageHandler: Image => Unit,
             shiftHandler: => Long, timeUpdater: (Long, Int) => Unit) =
    new SimpleVideoPlayer(url, telemetry, imageHandler, shiftHandler, timeUpdater)

  def createExperimental(url: String, telemetry: Telemetry, imageHandler: Image => Unit,
                         shiftHandler: => Long, timeUpdater: (Long, Int) => Unit) =
    new ExperimentalVideoPlayer(url, telemetry, imageHandler, shiftHandler, timeUpdater)
}
