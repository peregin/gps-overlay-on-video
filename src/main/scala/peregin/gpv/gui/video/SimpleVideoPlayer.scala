package peregin.gpv.gui.video

import java.awt.Image
import java.awt.image.BufferedImage

import com.xuggle.mediatool.ToolFactory
import com.xuggle.xuggler.ICodec.Type._
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Logging, TimePrinter}

import scala.concurrent._


trait SimpleVideoPlayerFactory extends VideoPlayerFactory {
  override def createPlayer(url: String, telemetry: Telemetry, imageHandler: Image => Unit,
                            shiftHandler: => Long, timeUpdater: (Long, Double) => Unit) =
    new SimpleVideoPlayer(url, telemetry, imageHandler, shiftHandler, timeUpdater)
}

class SimpleVideoPlayer(url: String, telemetry: Telemetry,
                  imageHandler: Image => Unit, shiftHandler: => Long,
                  timeUpdater: (Long, Double) => Unit) extends VideoPlayer with Logging {

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

  val overlay = new VideoOverlay(telemetry, imageHandler, shiftHandler)
  reader.addListener(overlay)

  val controller = new VideoController(timeUpdater, durationInMillis, realTime = true)
  overlay.addListener(controller)

  import scala.concurrent.ExecutionContext.Implicits.global
  future {
    while(running && reader.readPacket() == null) {
      // runs in a loop until the end
    }
  }

  override def play() = sys.error("not supported")

  override def pause() = sys.error("not supported")

  override def seek(percentage: Double) = sys.error("not supported")

  override def close() {
    running = false
    reader.close()
  }
}
