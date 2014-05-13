package peregin.gpv.manual

import peregin.gpv.Setup
import peregin.gpv.model.Telemetry
import java.io.File
import com.xuggle.mediatool.ToolFactory
import java.awt.image.BufferedImage
import peregin.gpv.gui.video.VideoOverlay
import java.awt.Image


object ConverterManualTest extends App {

  val file = "/Users/levi/water.json"
  val out = "/Users/levi/water-gps.mp4"
  val setup = Setup.loadFile(file)
  val telemetry = Telemetry.load(new File(setup.gpsPath.getOrElse(sys.error("gps file is not configured"))))
  val reader = ToolFactory.makeReader(setup.videoPath.getOrElse("video file is not configured"))
  reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)

  val writer = ToolFactory.makeWriter(out, reader)
  val overlay = new VideoOverlay(telemetry, (image: Image) => {}, setup.shift, debug = false)
  reader.addListener(overlay)
  overlay.addListener(writer)
  // add a viewer to the writer, to see media modified media
  //writer.addListener(ToolFactory.makeViewer())

  while(reader.readPacket == null) {
    // running in a loop
  }
}
