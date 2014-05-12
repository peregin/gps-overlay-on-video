package peregin.gpv.gui.video

import com.xuggle.mediatool.ToolFactory
import java.awt.Image
import java.awt.image.BufferedImage
import peregin.gpv.model.Telemetry
import scala.concurrent._
import com.xuggle.xuggler.IContainer
import peregin.gpv.util.{DurationPrinter, Logging}


class VideoPlayer(url: String, telemetry: Telemetry,
                  imageHandler: Image => Unit,
                  timeHandler: (Long, Int) => Unit) extends Logging {

  @volatile var running = true

  val reader = ToolFactory.makeReader(url)
  reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)

  reader.open()

  val container = reader.getContainer
  val durationInMillis = container.getDuration / 1000
  val bitRate = container.getBitRate
  info(s"duration: ${DurationPrinter.print(durationInMillis)}")

  val overlay = new VideoOverlay(telemetry, imageHandler, true)
  reader.addListener(overlay)

  val controller = new VideoController(timeHandler, durationInMillis)
  overlay.addListener(controller)

  import ExecutionContext.Implicits.global
  future {
    while(running && reader.readPacket() == null) {
      // runs in a loop until the end
    }
  }

  def seek(percentage: Double) {
    val p = percentage match {
      case a if a > 100d => 100d
      case b if b < 0d => 0d
      case c => percentage
    }

    val container = reader.getContainer
    val f = p * durationInMillis / 100
    val fInMicro = f.toLong * 1000

    // TODO: retrieve video stream index
    val stream = 0
    //val frameRate = container.getStream(stream).getStreamCoder.getFrameRate().getDouble()
    //val jumpTo = durationInMillis * frameRate * f

    container.seekKeyFrame(stream, fInMicro, IContainer.SEEK_FLAG_FRAME)
  }

  def close() {
    running = false
    reader.close()
  }
}
