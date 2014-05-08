package peregin.gpv.gui.video

import com.xuggle.mediatool.IMediaReader
import com.xuggle.xuggler.ICodec
import peregin.gpv.util.Logging


object VideoInfo extends Logging {

  def logInfo(mr: IMediaReader) {
    // show some video info
    val container = mr.getContainer
    log.info(s"duration = ${container.getDuration/1000} millis")
    log.info(s"streams = ${container.getNumStreams}")
    log.info(s"bit rate = ${container.getBitRate}")
    log.info(s"start time = ${container.getStartTime/1000} millis")
    import ICodec.Type._
    (0 until container.getNumStreams).map(container.getStream(_).getStreamCoder).filter(_.getCodecType == CODEC_TYPE_VIDEO).foreach{coder =>
      log.info(s"size [${coder.getWidth}, ${coder.getHeight}")
      log.info(s"format: ${coder.getPixelType}")
      log.info(f"frame-rate: ${coder.getFrameRate.getDouble}%5.2f")
    }
  }
}
