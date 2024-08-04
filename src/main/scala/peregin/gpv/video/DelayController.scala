package peregin.gpv.video

import peregin.gpv.util.Logging

trait DelayController extends Logging {

  @volatile private var prevVideoTs: Option[Long] = None
  @volatile private var prevClockTs: Option[Long] = None

  def resetDelay(): Unit = {
    prevVideoTs = None
    prevClockTs = None
  }

  def markDelay(videoTsInMillis: Long): Long = {
    val now = System.currentTimeMillis()
    var delay = (prevVideoTs, prevClockTs) match {
      case (Some(prevVideoTsInMillis), Some(prevClockTsInMillis)) =>
        val elapsedVideo = videoTsInMillis - prevVideoTsInMillis
        val elapsedClock = now - prevClockTsInMillis
        // and we give ourselves 50 ms of tolerance
        elapsedVideo - elapsedClock
      case _ => 0 // ignore not initialized state
    }
    prevVideoTs = Some(videoTsInMillis)
    // allow up to 300 ms lags in video processing.  If lower, reset back to 0
    if (delay < -300) {
      delay = 0
    }
    else if (delay > 1000) {
      delay = 0
    }
    prevClockTs = Some(now + delay)
    delay.max(0)
  }

  def waitIfNeeded(videoTsInMillis: Long): Unit = {
    val delay = markDelay(videoTsInMillis)
    if (delay > 0) {
      //debug(s"ts = ${TimePrinter.printDuration(videoTsInMillis)}, prevVideoTs = ${TimePrinter.printDuration(prevVideoTs)}, prevClockTs = ${TimePrinter.printTime(prevClockTs)}")
      //debug(s"wait for: ${TimePrinter.printDuration(delayInMillis)}")
      Thread.sleep(delay)
    }
  }
}
