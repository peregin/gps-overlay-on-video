package peregin.gpv.manual

import peregin.gpv.Setup
import peregin.gpv.model.Telemetry
import java.io.File
import com.xuggle.mediatool.ToolFactory
import java.awt.image.BufferedImage
import peregin.gpv.gui.video.{VideoController, VideoOverlay}
import java.awt.Image
import peregin.gpv.util.{DurationPrinter, Logging}


object ConverterManualTest extends App with Logging {

  val file = "/Users/levi/zugerberg.json"
  val out = "/Users/levi/zugerberg-gps.mp4"
  val setup = Setup.loadFile(file)
  val telemetry = Telemetry.load(new File(setup.gpsPath.getOrElse(sys.error("gps file is not configured"))))
  val reader = ToolFactory.makeReader(setup.videoPath.getOrElse("video file is not configured"))
  reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
  reader.open()
  val container = reader.getContainer
  val durationInMillis = container.getDuration / 1000
  info(s"duration: ${DurationPrinter.print(durationInMillis)}")
  var mark = 0L


  val writer = ToolFactory.makeWriter(out, reader)
  val overlay = new VideoOverlay(telemetry, (image: Image) => {}, setup.shift, debug = false)
  val controller = new VideoController(timeHandler, durationInMillis, realTime = false)
  reader.addListener(overlay)
  overlay.addListener(controller)
  controller.addListener(writer)
  // add a viewer to the writer, to see media modified media
  //writer.addListener(ToolFactory.makeViewer())

  while(reader.readPacket == null) {
    // running in a loop
  }

  def timeHandler(videoTsInMillis: Long, percentage: Int) {
    val tick = System.currentTimeMillis
    if (tick - mark > 2000) {
      info(s"% = $percentage videoTs = ${DurationPrinter.print(videoTsInMillis)}")
      mark = tick
    }
  }
}
