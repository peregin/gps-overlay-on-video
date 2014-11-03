package peregin.gpv.gui.video

import java.awt.Image

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IVideoPictureEvent
import peregin.gpv.model.Telemetry
import peregin.gpv.util.Logging


class VideoOverlay(telemetry: Telemetry, imageHandler: Image => Unit, shiftHandler: () => Long) extends MediaToolAdapter
  with Logging with OverlayPainter {

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val ts = event.getTimeStamp
    val unit = event.getTimeUnit
    val tsInMillis = unit.toMillis(ts)

    val image = event.getImage

    paintGauges(telemetry, tsInMillis, image, shiftHandler())

    imageHandler(image)

    super.onVideoPicture(event)
  }
}
