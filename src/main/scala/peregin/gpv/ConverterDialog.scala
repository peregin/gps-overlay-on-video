package peregin.gpv

import java.awt.image.BufferedImage
import java.awt.{Font, Color, Dimension}
import java.io.File
import javax.swing.BorderFactory
import javax.swing.border.{BevelBorder, Border}

import com.xuggle.mediatool.ToolFactory
import peregin.gpv.gui._
import peregin.gpv.gui.gauge.DashboardPainter
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Logging, TimePrinter}
import peregin.gpv.video.{VideoOverlay, VideoPlayer}

import scala.swing._
import scala.swing.event.ButtonClicked


class ConverterDialog(setup: Setup, telemetry: Telemetry, parent: Window = null) extends Dialog(parent)
  with VideoPlayer.Listener with DashboardPainter with Logging {

  title = "Converter"
  modal = true
  preferredSize = new Dimension(400, 300)

  private val imagePanel = new ImagePanel
  private val generateButton = new Button("Generate")
  private val closeButton = new Button("Cancel")
  private val progress = new ProgressBar
  private val status = new Label

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
      add(closeButton, "sg 1, w 80, center, wrap")
    }, "wrap")
    add(status, "span 2, center, growx")
  }

  progress.min = 0
  progress.max = 100
  progress.value = 0

  status.foreground = Color.white
  status.background = Color.blue
  status.opaque = true
  status.font = status.font.deriveFont(Font.BOLD)
  status.text = "Ready"
  status.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)

  Goodies.mapEscapeTo(this, handleClose)

  listenTo(generateButton, closeButton)
  reactions += {
    case ButtonClicked(`generateButton`) => handleGenerate()
    case ButtonClicked(`closeButton`) => handleClose()
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

      Swing.onEDT {
        status.text = "Generating video file..."
        generateButton.enabled = false
      }

      while(reader.readPacket == null) {
        // running in a loop
      }
    }

    converterFuture onSuccess {
      case _ => Swing.onEDT {
        status.background = Color.green
        status.text = "Success"
        closeButton.text = "Close"
      }
    }

    converterFuture onFailure {
      case f => Swing.onEDT {
        status.background = Color.red
        status.text = s"Failed: ${f.getMessage}"
        closeButton.text = "Close"
      }
    }
  }

  private def handleClose() {
    log.info("cancel or close")
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