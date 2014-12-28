package peregin.gpv.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.{VideoPictureEvent, IVideoPictureEvent}
import com.xuggle.xuggler.{IVideoPicture, IVideoResampler}

/**
 * Re-codes the media file as needed.
 *
 * Recommended resolutions for youtube:
 * <ul>
 *   <li>2160p: 3840x2160
 *   <li>1440p: 2560x1440
 *   <li>1080p: 1920x1080
 *   <li>720p: 1280x720
 *   <li>480p: 854x480
 *   <li>360p: 640x360
 *   <li>240p: 426x240
 * </ul>
 *
 * Recommended audio format:
 * Codec: AAC-LC
 * Channels: Stereo or Stereo + 5.1
 * Sample rate 96khz or 48khz
 */
class PictureResizer(dWidth: Int, dHeight: Int) extends MediaToolAdapter {

  private var videoResampler: Option[IVideoResampler] = None

  override def onVideoPicture(event: IVideoPictureEvent) {
    val pic = event.getPicture

    if (videoResampler.isEmpty) {
      videoResampler = Some(IVideoResampler.make(dWidth, dHeight,
        pic.getPixelType, pic.getWidth, pic.getHeight, pic.getPixelType))
    }
    val out = IVideoPicture.make(pic.getPixelType, dWidth, dHeight)
    videoResampler.foreach(_.resample(out, pic))

    val asc = new VideoPictureEvent(event.getSource, out, event.getStreamIndex)
    super.onVideoPicture(asc)
    out.delete()
  }
}
