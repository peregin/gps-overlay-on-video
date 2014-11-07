package peregin.gpv.gui

import java.awt.image.BufferedImage
import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter

import peregin.gpv.Setup
import peregin.gpv.video.{VideoPlayer, VideoPlayerFactory}
import peregin.gpv.util.{Logging, TimePrinter}

import scala.swing.event.ValueChanged
import scala.swing.{Label, Swing}


class VideoPanel(openVideoHandler: File => Unit, listener: VideoPlayer.Listener)
  extends MigPanel("ins 2", "", "[fill]") with VideoPlayer.Listener with Logging {
  self: VideoPlayerFactory =>

  val chooser = new FileChooserPanel("Load video file:", openVideoHandler, new FileNameExtensionFilter("Video files (mp4)", "mp4"))
  add(chooser, "pushx, growx, wrap")

  val imagePanel = new ImagePanel
  add(imagePanel, "grow, pushy, wrap")

  val elapsed = new Label(s"${TimePrinter.printDuration(0)}")
  val duration = new Label(s"${TimePrinter.printDuration(0)}")
  val slider = new PercentageSlider
  val startStopButton = new StartStopButton("images/play.png", "Play", "images/pause.png", "Pause", playOrPauseVideo())
  val controlPanel = new MigPanel("ins 0", "", "") {
    val progress = new MigPanel("ins 0", "", "") {
      add(elapsed, "pushy, wrap")
      add(duration, "pushy")
    }
    add(progress, "pushy")
    add(slider, "pushx, growx")
    add(startStopButton, "align right")
    add(new ImageButton("images/forward.png", "Step", stepForwardVideo()), "align right")
  }
  add(controlPanel, "growx")

  listenTo(slider)
  reactions += {
    case ValueChanged(`slider`) => player.foreach(_.seek(slider.percentage))
  }

  @volatile var player: Option[VideoPlayer] = None

  def refresh(setup: Setup) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    setup.videoPath.foreach{path =>
      player.foreach(_.close())
      player = Some(createPlayer(path, this))
    }
  }

  def playOrPauseVideo() {
    player.foreach{p =>
      if (startStopButton.isPlaying) p.pause() else p.play()
    }
  }

  def stepForwardVideo() {
    player.foreach(_.step())
  }

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage) {
    // first allow the listeners to modify the image, then show it
    listener.videoEvent(tsInMillis, percentage, image)
    Swing.onEDT{
      imagePanel.show(image)
      slider.percentage = percentage
      elapsed.text = s"${TimePrinter.printDuration(tsInMillis)}"
      duration.text = s"${TimePrinter.printDuration(player.map(_.duration))}"
    }
  }

  override def videoStopped() {
    listener.videoStopped()
    startStopButton.stop()
    log.info("stopped")
  }

  override def videoStarted() {
    listener.videoStopped()
    startStopButton.play()
    log.info("started")
  }
}
