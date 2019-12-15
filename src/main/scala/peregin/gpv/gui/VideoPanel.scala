package peregin.gpv.gui

import java.awt.image.BufferedImage
import java.io.File

import peregin.gpv.Setup
import peregin.gpv.util.{Io, Logging, TimePrinter}
import peregin.gpv.video.{VideoPlayer, VideoPlayerFactory}

import scala.swing.event.ValueChanged
import scala.swing.{Label, Swing}


class VideoPanel(openVideoHandler: File => Unit, listener: VideoPlayer.Listener)
  extends MigPanel("ins 2", "", "[fill]") with VideoPlayer.Listener with Logging {
  self: VideoPlayerFactory =>

  val chooser = new FileChooserPanel("Load video file:", openVideoHandler, ExtensionFilters.video)
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
    case SliderChanged(`slider`, percentage) => player.foreach(_.seek(percentage))
  }

  @volatile private var player: Option[VideoPlayer] = None
  @volatile private var lastRawImage: Option[BufferedImage] = None // without overlay

  def refresh(setup: Setup): Unit = {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    lastRawImage = None
    setup.videoPath.foreach{path =>
      player.foreach(_.close())
      player = Some(createPlayer(path, this))
    }
  }

  def playOrPauseVideo(): Unit = {
    player.foreach{p =>
      if (startStopButton.isPlaying) p.pause() else p.play()
    }
  }

  def stepForwardVideo(): Unit = {
    player.foreach(_.step())
  }

  def fireLastVideoEventIfNotPlaying(): Unit = {
    if (!player.exists(_.playing)) {
      log.debug("refreshing dashboard painter, because player is not running")
      // when player is not running and changing the transparency
      lastRawImage.foreach { rawImage =>
        val ts = TimePrinter.text2Duration(elapsed.text)
        val total = TimePrinter.text2Duration(duration.text)
        val percentage = (ts * 100).toDouble / total
        videoEvent(ts, percentage, rawImage)
      }
    }
  }

  override def seekEvent(percentage: Double): Unit = {
    slider.percentage = percentage
  }

  override def videoEvent(tsInMillis: Long, percentage: Double, image: BufferedImage): Unit = {
    lastRawImage = Some(image)
    val topImage = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB)
    // first allow the listeners to modify the image, then show it
    listener.videoEvent(tsInMillis, percentage, topImage)
    Swing.onEDT{
      imagePanel.show(image, Some(topImage))
      slider.percentage = percentage
      elapsed.text = s"${TimePrinter.printDuration(tsInMillis)}"
      duration.text = s"${TimePrinter.printDuration(player.map(_.duration))}"
    }
  }

  override def videoStopped(): Unit = {
    listener.videoStopped()
    startStopButton.stop()
    log.info("stopped")
  }

  override def videoStarted(): Unit = {
    listener.videoStopped()
    startStopButton.play()
    log.info("started")
  }
}
