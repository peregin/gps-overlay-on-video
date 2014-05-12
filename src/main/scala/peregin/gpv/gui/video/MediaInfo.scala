package peregin.gpv.gui.video

import com.xuggle.mediatool.IMediaReader
import com.xuggle.xuggler.ICodec
import peregin.gpv.util.Logging


object MediaInfo extends Logging {

  def logInfo(mr: IMediaReader) {
    // show some video info
    val container = mr.getContainer
    info(s"duration = ${container.getDuration/1000} millis")
    info(s"streams = ${container.getNumStreams}")
    info(s"bit rate = ${container.getBitRate}")
    info(s"start time = ${container.getStartTime/1000} millis")
    import ICodec.Type._
    (0 until container.getNumStreams).map(container.getStream(_).getStreamCoder).filter(_.getCodecType == CODEC_TYPE_VIDEO).foreach{coder =>
      info(s"size [${coder.getWidth}, ${coder.getHeight}")
      info(s"format: ${coder.getPixelType}")
      info(f"frame-rate: ${coder.getFrameRate.getDouble}%5.2f")
    }
  }
}
