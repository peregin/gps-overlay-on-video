package peregin.gpv.video

import peregin.gpv.util.{TimePrinter, Logging}

trait DelayController extends Logging {

  @volatile private var prevVideoTs: Option[Long] = None
  @volatile private var prevClockTs: Option[Long] = None

  def reset() {
    prevVideoTs = None
    prevClockTs = None
  }

  def markDelay(videoTsInMillis: Long): Long = {
    val now = System.currentTimeMillis()
    prevVideoTs = Some(videoTsInMillis)
    prevClockTs = Some(now)
    (prevVideoTs, prevClockTs) match {
      case (Some(prevVideoTsInMillis), Some(prevClockTsInMillis)) =>
        val elapsedVideo = videoTsInMillis - prevVideoTsInMillis
        val elapsedClock = now - prevClockTsInMillis
        // and we give ourselves 50 ms of tolerance
        elapsedVideo - elapsedClock + 50l
      case _ => 0 // ignore not initialized state
    }
  }

  def waitIfNeeded(videoTsInMillis: Long) {
    val delay = markDelay(videoTsInMillis)
    if (delay > 0) {
      //debug(s"ts = ${TimePrinter.printDuration(videoTsInMillis)}, prevVideoTs = ${TimePrinter.printDuration(prevVideoTs)}, prevClockTs = ${TimePrinter.printTime(prevClockTs)}")
      //debug(s"wait for: ${TimePrinter.printDuration(delayInMillis)}")
      Thread.sleep(delay)
    }
  }
}
