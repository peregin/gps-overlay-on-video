package peregin.gpv.gui.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent


class VideoController extends MediaToolAdapter {

  override def onVideoPicture(event: IVideoPictureEvent) = {
    super.onVideoPicture(event)
  }

  def waitIfNeeded() {
    Thread.sleep(30)
  }
}
