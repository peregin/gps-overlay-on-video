package peregin.gpv.manual

import java.awt.image.BufferedImage
import java.io.File

import com.xuggle.mediatool.ToolFactory
import peregin.gpv.Setup
import peregin.gpv.gui.gauge.DashboardPainter
import peregin.gpv.video.{VideoOverlay, VideoPlayer}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Logging, TimePrinter}


object ConverterManualTest extends App with DashboardPainter with VideoPlayer.Listener with Logging {

  val file = "/Users/levi/seefeld.json"
  val out = "/Users/levi/seefeld-gps.mp4"
  val setup = Setup.loadFile(file)
  val telemetry = Telemetry.load(new File(setup.gpsPath.getOrElse(sys.error("gps file is not configured"))))
  val reader = ToolFactory.makeReader(setup.videoPath.getOrElse("video file is not configured"))
  reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
  reader.open()
  val container = reader.getContainer
  val durationInMillis = container.getDuration / 1000
  info(s"duration: ${TimePrinter.printDuration(durationInMillis)}")
  var mark = 0L


  val writer = ToolFactory.makeWriter(out, reader)
  val overlay = new VideoOverlay(this, durationInMillis)
  reader.addListener(overlay)
  overlay.addListener(writer)
  // add a viewer to the writer, to see media modified media
  //writer.addListener(ToolFactory.makeViewer())

  while(reader.readPacket == null) {
    // running in a loop
  }

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage) {
    paintGauges(telemetry, tsInMillis, image, setup.shift)

    val tick = System.currentTimeMillis
    if (tick - mark > 2000) {
      info(s"% = $percentage videoTs = ${TimePrinter.printDuration(tsInMillis)}")
      mark = tick
    }
  }

  override def videoStarted() {}

  override def videoStopped() {}
}
