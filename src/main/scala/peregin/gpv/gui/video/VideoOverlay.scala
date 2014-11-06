package peregin.gpv.gui.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent


class VideoOverlay(listener: VideoPlayer.Listener, durationInMillis: Long) extends MediaToolAdapter {

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val tsInMillis = event.getTimeUnit.toMillis(event.getTimeStamp)
    val percentage = if (durationInMillis > 0) tsInMillis * 100 / durationInMillis else 0
    val image = event.getImage
    listener.videoEvent(tsInMillis, percentage, image)

    super.onVideoPicture(event)
  }
}
