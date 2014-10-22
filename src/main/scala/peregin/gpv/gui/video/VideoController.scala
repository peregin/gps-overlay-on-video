package peregin.gpv.gui.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent
import peregin.gpv.util.{TimePrinter, Logging}


class VideoController(timeUpdater: (Long, Int) => Unit, durationInMillis: Long, realTime: Boolean) extends MediaToolAdapter with Logging {
  
  @volatile private var prevVideoTs: Option[Long] = None
  @volatile private var prevClockTs: Option[Long] = None

  def reset() {
    prevVideoTs = None
    prevClockTs = None
    debug("reset")
  }

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val tsInMillis = event.getTimeUnit.toMillis(event.getTimeStamp)
    val percentage = if (durationInMillis > 0) tsInMillis * 100 / durationInMillis else 0
    timeUpdater(tsInMillis, percentage.toInt)

    if (realTime) waitIfNeeded(tsInMillis)

    super.onVideoPicture(event)
  }

  private def waitIfNeeded(videoTsInMillis: Long) {
    val now = System.currentTimeMillis()
    (prevVideoTs, prevClockTs) match {
      case (Some(prevVideoTsInMillis), Some(prevClockTsInMillis)) =>
        val elapsedVideo = videoTsInMillis - prevVideoTsInMillis
        val elapsedClock = now - prevClockTsInMillis
        val delayInMillis = elapsedVideo - elapsedClock
        if (delayInMillis > 0) {
          debug(s"ts = ${TimePrinter.printDuration(videoTsInMillis)}, prevVideoTs = ${TimePrinter.printDuration(prevVideoTs)}, prevClockTs = ${TimePrinter.printTime(prevClockTs)}")
          debug(s"wait for: ${TimePrinter.printDuration(delayInMillis)}")
          Thread.sleep(delayInMillis)
        }
      case _ => // ignore not initialized state
    }

    prevVideoTs = Some(videoTsInMillis)
    prevClockTs = Some(now)
  }
}
