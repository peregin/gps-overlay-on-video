package peregin.gpv.video

import com.xuggle.mediatool.MediaToolAdapter
import com.xuggle.mediatool.event.IAddStreamEvent
import com.xuggle.xuggler.ICodec

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
class StreamResizer(dWidth: Int, dHeight: Int) extends MediaToolAdapter {

  override def onAddStream(event: IAddStreamEvent) {
    val streamIndex = event.getStreamIndex.toLong
    val streamCoder = event.getSource.getContainer.getStream(streamIndex).getStreamCoder
    streamCoder.getCodecType match {
      case ICodec.Type.CODEC_TYPE_VIDEO =>
        streamCoder.setWidth(dWidth)
        streamCoder.setHeight(dHeight)
      case _ => // ignore
    }
    super.onAddStream(event)
  }
}
