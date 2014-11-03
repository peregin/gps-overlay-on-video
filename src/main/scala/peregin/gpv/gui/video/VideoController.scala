package peregin.gpv.gui.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent


class VideoController(timeUpdater: (Long, Double) => Unit, durationInMillis: Long, realTime: Boolean)
  extends MediaToolAdapter with DelayController {

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val tsInMillis = event.getTimeUnit.toMillis(event.getTimeStamp)
    val percentage = if (durationInMillis > 0) tsInMillis * 100 / durationInMillis else 0
    timeUpdater(tsInMillis, percentage)

    if (realTime) waitIfNeeded(tsInMillis)

    super.onVideoPicture(event)
  }
}
