package peregin.gpv.gui.video

import java.awt.Image

/**
 * Created by peregin on 03/07/14.
 */
trait VideoListener {

  // invoked when a new image has been produced
  def frameEvent(img: Image)

  // in milliseconds
  def shiftGpsWith: Long

  // timestamp in millis
  def playProgress(timestamp: Long, percentage: Int)
}
