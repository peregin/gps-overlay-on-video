package peregin.gpv

import org.bytedeco.javacv.{FFmpegFrameGrabber, FFmpegFrameRecorder, Frame, Java2DFrameConverter}

import java.awt.image.BufferedImage
import java.awt.{Color, Dimension, Font}
import java.io.File
import javax.swing.{BorderFactory, JSlider}
import javax.swing.border.BevelBorder
import peregin.gpv.gui.TemplatePanel.TemplateEntry
import peregin.gpv.gui._
import peregin.gpv.gui.dashboard.DashboardPainter
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Logging, TimePrinter}
import peregin.gpv.video.VideoPlayer

import scala.swing.Swing.EmptyIcon
import scala.swing.{Alignment, Button, Dialog, Label, ProgressBar, Separator, Swing, Window}
import scala.swing.event.ButtonClicked
import scala.util.{Failure, Success}


class ConverterDialog(setup: Setup, telemetry: Telemetry, template: TemplateEntry, parent: Window = null) extends Dialog(parent)
  with VideoPlayer.Listener with DashboardPainter with Logging {

  title = "Converter"
  modal = true
  preferredSize = new Dimension(400, 440)
  // setup painter
  dash = template.dashboard

  private val imagePanel = new ImagePanel
  private val generateButton = new Button("Generate")
  private val closeButton = new Button("Cancel")
  private val progressBar = new ProgressBar
  private val statusLabel = new Label
  private val durationLabel = new Label

  private var mark = 0L // measures the generation time

  private val bitrateSelection = new JSlider(0, 10000, setup.bitrateRatio.getOrElse(10000));
  bitrateSelection.addChangeListener(event => updateBitrate(bitrateSelection.getValue))
  private val chooser = new FileChooserPanel("File name of the new video:", save, ExtensionFilters.video, false)
  chooser.fileInput.text = setup.outputPath.getOrElse("")

  private val dashboardLabel = new Label(s"Dashboard: ${template.name}", EmptyIcon, Alignment.Left)

  private var startTimeMs: Long = _;
  private var stopped = false;

  contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
    add(imagePanel, "grow, pushy, wrap")
    add(new MigPanel("ins 0, fill", "[fill]", "") {
      add(progressBar, "growx, pushx")
      add(durationLabel, "align right")
    }, "wrap")
    add(new Separator(), "growx, wrap")
    add(dashboardLabel, "align left, wrap")
    add(new Separator(), "growx, wrap")
    add(new MigPanel("ins 2, align center", "[center]", "") {
      add(new Label("Bitrate ratio:"), "pushx, align left")
      add(bitrateSelection, "pushx, growx")
    }, "wrap")
    add(new Separator(), "growx, wrap")
    add(chooser, "wrap")
    add(new Separator(), "growx, wrap")
    add(new MigPanel("ins 5, align center", "[center]", "") {
      add(generateButton, "sg 1, w 80, center")
      add(closeButton, "sg 1, w 80, center")
    }, "wrap")
    add(statusLabel, "span 2, center, growx")
  }

  progressBar.min = 0
  progressBar.max = 100
  progressBar.value = 0
  progressBar.labelPainted = true
  progressBar.label = s"${TimePrinter.printDuration(0)}"

  durationLabel.text = s"${TimePrinter.printDuration(0)}"

  statusLabel.foreground = Color.white
  statusLabel.background = Color.blue
  statusLabel.opaque = true
  statusLabel.font = statusLabel.font.deriveFont(Font.BOLD)
  statusLabel.text = "Ready"
  statusLabel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)

  dashboardLabel.background = Color.black
  dashboardLabel.foreground = Color.white
  dashboardLabel.opaque = true
  dashboardLabel.font = dashboardLabel.font.deriveFont(Font.BOLD)
  dashboardLabel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)

  Goodies.mapEscapeTo(this, () => handleClose())

  listenTo(generateButton, closeButton)
  reactions += {
    case ButtonClicked(`generateButton`) => handleGenerate()
    case ButtonClicked(`closeButton`) => handleClose()
  }

  def save(file: File): Unit = {
    val path = file.getAbsolutePath
    setup.outputPath = Some(path)
    chooser.fileInput.text = path
    log.info(s"save to $path")
  }

  def updateBitrate(bitrate10k: Int) = {
    setup.bitrateRatio = Some(bitrate10k)
  }

  private def handleGenerate(): Unit = {
    log.info("generate...")
    mark = 0L

    val videoInputFile = setup.videoPath.getOrElse("video file is not configured")
    val videoOutputFile = setup.outputPath.getOrElse("output file is not provided")

    log.info(s"reading video file from $videoInputFile")
    val reader = new FFmpegFrameGrabber(videoInputFile)
    reader.start()
    val durationInMillis = reader.getLengthInTime / 1000
    durationLabel.text = s"${TimePrinter.printDuration(durationInMillis)}"

    log.info(s"video output file to $videoOutputFile")
    val writer = createWriterFromReader(videoOutputFile, reader)

    startTimeMs = System.currentTimeMillis()

    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent._
    val converterFuture = Future {

      Swing.onEDT {
        statusLabel.text = "Generating video file..."
        generateButton.enabled = false
      }

      convertVideoBody(
        reader,
        writer,
        (timeInMs, image) => videoEvent(timeInMs, timeInMs * 100.0 / durationInMillis, image),
        () => stopped
      )
      writer.flush()
      writer.close()
      reader.close()
    }

    converterFuture onComplete {
      case Success(_) => Swing.onEDT {
        statusLabel.background = Color.green
        statusLabel.text = "Success"
        closeButton.text = "Close"
        progressBar.value = 100
        progressBar.label = s"${TimePrinter.printDuration(durationInMillis)}"
      }
      case Failure(f) => Swing.onEDT {
        statusLabel.background = Color.red
        statusLabel.text = s"Failed: ${f.getMessage}"
        closeButton.text = "Close"
      }
    }
  }

  private def handleClose(): Unit = {
    log.info("cancel or close")
    close()
    stopped = true;
  }

  private def createWriterFromReader(filename: String, grabber: FFmpegFrameGrabber): FFmpegFrameRecorder = {
    val recorder = new FFmpegFrameRecorder(filename, grabber.getImageWidth, grabber.getImageHeight, grabber.getAudioChannels)
    recorder.setFormat("mp4")
    recorder.setOption("threads", "4")
    recorder.setOption("c", "copy")
    recorder.setFrameRate(grabber.getFrameRate)

    if (grabber.hasVideo) {
      recorder.setVideoMetadata(grabber.getVideoMetadata)
      recorder.setVideoCodec(grabber.getVideoCodec)
      recorder.setVideoBitrate((grabber.getVideoBitrate * (setup.bitrateRatio.getOrElse(10000) / 10000.0)).toInt)
      recorder.setVideoOption("tune", "film")
      recorder.setVideoOption("threads", "4")
      recorder.setVideoOption("frame-threads", "4")
      recorder.setVideoOption("threadslice", "4")
      recorder.setVideoOption("slicethread", "4")
      recorder.setVideoOption("thread_slice", "4")
      recorder.setVideoOption("slice_thread", "4")
      //recorder.setVideoOption("preset", "ultrafast")

    }
    if (grabber.hasAudio) {
      recorder.setAudioChannels(grabber.getAudioChannels)
      recorder.setAudioMetadata(grabber.getAudioMetadata)
      recorder.setAudioCodec(grabber.getAudioCodec)
      recorder.setAudioBitrate(grabber.getAudioBitrate)
    }

    recorder.start()
    recorder
  }

  private def convertVideoBody(
                                grabber: FFmpegFrameGrabber,
                                recorder: FFmpegFrameRecorder,
                                painter: (Long, BufferedImage) => Unit,
                                stopIndicator: () => Boolean
                              ): Boolean = {
    var frame: Frame = null
    var bufferedImage: BufferedImage = null
    var imageType = -1
    var stopped: Boolean = false;
    while ({ stopped = stopIndicator(); !stopped } && { frame = grabber.grab; frame != null }) {
      if (frame.`type` eq Frame.Type.VIDEO) {
        if (imageType != Java2DFrameConverter.getBufferedImageType(frame)) {
          imageType = Java2DFrameConverter.getBufferedImageType(frame)
          bufferedImage = new BufferedImage(grabber.getImageWidth, grabber.getImageHeight, imageType)
        }
        // Convert OpenCV frame to BufferedImage
        Java2DFrameConverter.copy(frame, bufferedImage)
        // Paint
        painter(frame.timestamp / 1000, bufferedImage)
        // Convert BufferedImage back to OpenCV frame
        val modifiedFrame = frame.clone
        Java2DFrameConverter.copy(bufferedImage, modifiedFrame)
        // Record the modified frame
        recorder.record(modifiedFrame)
      }
      else {
        recorder.record(frame)
      }
    }

    !stopped
  }

  override def seekEvent(percentage: Double): Unit = {}

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage): Unit = {
    paintGauges(telemetry, tsInMillis, image, setup.shift, setup.transparency, setup.units)

    val tick = System.currentTimeMillis
    if (tick - mark >= 2000) {

      Swing.onEDT {
        val speed: Double = tsInMillis / Math.max(System.currentTimeMillis() - startTimeMs, 1.0)
        progressBar.value = percentage.toInt
        imagePanel.show(image)
        progressBar.label = f"${TimePrinter.printDuration(tsInMillis)}   ${speed}%.3f×"
      }

      mark = tick
    }
  }

  override def videoStarted(): Unit = {}

  override def videoStopped(): Unit = {}
}
