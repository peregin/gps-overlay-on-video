package peregin.gpv

import java.awt.Dimension
import java.awt.image.BufferedImage
import java.io.File

import com.xuggle.mediatool.ToolFactory
import peregin.gpv.gui._
import peregin.gpv.gui.gauge.DashboardPainter
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{TimePrinter, Logging}
import peregin.gpv.video.{VideoPlayer, VideoOverlay}

import scala.swing.event.ButtonClicked
import scala.swing._


class ConverterDialog(setup: Setup, telemetry: Telemetry, parent: Window = null) extends Dialog(parent)
  with VideoPlayer.Listener with DashboardPainter with Logging {

  title = "Converter"
  modal = true
  preferredSize = new Dimension(400, 300)

  private val imagePanel = new ImagePanel
  private val generateButton = new Button("Generate")
  private val cancelButton = new Button("Cancel")
  private val progress = new ProgressBar
  progress.min = 0
  progress.max = 100
  progress.value = 0
  private var mark = 0L // measures the generation time

  private val chooser = new FileChooserPanel("File name of the new video:", save, ExtensionFilters.video, false)
  chooser.fileInput.text = setup.outputPath.getOrElse("")

  contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
    add(imagePanel, "grow, pushy, wrap")
    add(progress, "pushx, wrap")
    add(chooser, "wrap")
    add(new Separator(), "growx, wrap")
    add(new MigPanel("ins 5, align center", "[center]", "") {
      add(generateButton, "sg 1, w 80, center")
      add(cancelButton, "sg 1, w 80, center")
    }, "dock south")
  }

  Goodies.mapEscapeTo(this, handleCancel)

  listenTo(generateButton, cancelButton)
  reactions += {
    case ButtonClicked(`generateButton`) => handleGenerate()
    case ButtonClicked(`cancelButton`) => handleCancel()
  }

  def save(file: File) {
    setup.outputPath = Some(file.getAbsolutePath)
    log.info(s"save to ${file.getAbsolutePath}")
  }

  private def handleGenerate() {
    log.info("generate...")
    mark = 0L
    val reader = ToolFactory.makeReader(setup.videoPath.getOrElse("video file is not configured"))
    reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
    reader.open()
    val container = reader.getContainer
    val durationInMillis = container.getDuration / 1000
    info(s"duration: ${TimePrinter.printDuration(durationInMillis)}")

    val writer = ToolFactory.makeWriter(setup.outputPath.getOrElse("output file is not provided"), reader)
    val overlay = new VideoOverlay(this, durationInMillis)
    reader.addListener(overlay)
    overlay.addListener(writer)

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    val converterFuture = future {
      while(reader.readPacket == null) {
        // running in a loop
      }
    }
  }

  private def handleCancel() {
    log.info("cancel")
    close()
  }

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage) {
    paintGauges(telemetry, tsInMillis, image, setup.shift)

    val tick = System.currentTimeMillis
    if (tick - mark > 2000) {
      progress.value = percentage.toInt
      imagePanel.show(image)

      info(s"% = $percentage videoTs = ${TimePrinter.printDuration(tsInMillis)}")
      mark = tick
    }
  }

  override def videoStarted() {}

  override def videoStopped() {}
}