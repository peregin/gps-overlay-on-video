package peregin.gpv.gui.video

import peregin.gpv.util.{TimePrinter, Logging}

trait DelayController extends Logging {

  @volatile private var prevVideoTs: Option[Long] = None
  @volatile private var prevClockTs: Option[Long] = None

  def reset() {
    prevVideoTs = None
    prevClockTs = None
    debug("reset")
  }

  def waitIfNeeded(videoTsInMillis: Long) {
    val now = System.currentTimeMillis()
    (prevVideoTs, prevClockTs) match {
      case (Some(prevVideoTsInMillis), Some(prevClockTsInMillis)) =>
        val elapsedVideo = videoTsInMillis - prevVideoTsInMillis
        val elapsedClock = now - prevClockTsInMillis
        // TODO: val millisecondsTolerance = 50L // and we give ourselfs 50 ms of tolerance
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
