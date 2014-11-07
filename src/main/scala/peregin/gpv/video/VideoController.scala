package peregin.gpv.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent


class VideoController extends MediaToolAdapter with DelayController {

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val tsInMillis = event.getTimeUnit.toMillis(event.getTimeStamp)
    waitIfNeeded(tsInMillis)

    super.onVideoPicture(event)
  }
}
