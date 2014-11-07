package peregin.gpv.video

import java.awt.image.BufferedImage

import com.xuggle.mediatool.ToolFactory
import com.xuggle.xuggler.ICodec.Type._
import peregin.gpv.util.{Logging, TimePrinter}

import scala.concurrent._


trait SimpleVideoPlayerFactory extends VideoPlayerFactory {
  override def createPlayer(url: String, listener: VideoPlayer.Listener) =
    new SimpleVideoPlayer(url, listener)
}

class SimpleVideoPlayer(url: String, listener: VideoPlayer.Listener) extends VideoPlayer with Logging {

  @volatile var running = true

  val reader = ToolFactory.makeReader(url)
  reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)

  reader.open()

  val container = reader.getContainer
  val durationInMillis = container.getDuration / 1000
  val bitRate = container.getBitRate
  val (videoCoder, videoStreamIx) = (0 until container.getNumStreams).
    map(container.getStream(_).getStreamCoder).
    zipWithIndex.filter(_._1.getCodecType == CODEC_TYPE_VIDEO).
    head
  val frameRate = videoCoder.getFrameRate.getDouble
  info(s"duration: ${TimePrinter.printDuration(durationInMillis)}")
  info(s"bit rate: $bitRate")
  info(s"video stream: $videoStreamIx")
  info(f"frame rate: $frameRate%5.2f")
  info(s"size [${videoCoder.getWidth}, ${videoCoder.getHeight}")

  val overlay = new VideoOverlay(listener, durationInMillis)
  reader.addListener(overlay)

  val controller = new VideoController()
  overlay.addListener(controller)

  import scala.concurrent.ExecutionContext.Implicits.global
  future {
    while(running && reader.readPacket() == null) {
      // runs in a loop until the end
    }
  }

  override def play() = sys.error("not supported")

  override def step() = sys.error("not supported")

  override def pause() = sys.error("not supported")

  override def seek(percentage: Double) = sys.error("not supported")

  override def close() {
    running = false
    reader.close()
  }

  override def duration = durationInMillis
}
