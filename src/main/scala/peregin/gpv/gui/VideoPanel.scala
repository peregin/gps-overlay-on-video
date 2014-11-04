package peregin.gpv.gui

import java.awt.{Color, Graphics, Image}
import java.io.File
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

import peregin.gpv.Setup
import peregin.gpv.gui.video.{VideoPlayer, VideoPlayerFactory}
import peregin.gpv.model.Telemetry
import peregin.gpv.util.{Logging, TimePrinter}

import scala.swing.{Label, Swing}
import scala.swing.event.ValueChanged


class VideoPanel(openVideoHandler: File => Unit, videoTimeUpdater: Long => Unit, shiftHandler: () => Long)
  extends MigPanel("ins 2", "", "[fill]") with Logging {
  self: VideoPlayerFactory =>

  var telemetry = Telemetry.empty

  val chooser = new FileChooserPanel("Load video file:", openVideoHandler, new FileNameExtensionFilter("Video files (mp4)", "mp4"))
  add(chooser, "pushx, growx, wrap")

  class ImagePanel extends JPanel {
    var image: Image = null

    def show(im: Image) {
      image = im
      repaint()
    }

    override def paint(g: Graphics) = {
      val width = getWidth
      val height = getHeight
      g.setColor(Color.black)
      g.fillRect(0, 0, width, height)

      if (image != null) {
        val iw = image.getWidth(null)
        val ih = image.getHeight(null)

        // the image needs to be scaled to fit to the display area
        val (w, h) = if (iw > width || ih > height) {
          val scale = math.min(width.toDouble / iw, height.toDouble / ih)
          ((iw.toDouble * scale).toInt, (ih.toDouble * scale).toInt)
        } else (iw, ih)
        val x = (width - w) / 2
        val y = (height - h) / 2
        //debug(s"(w, h) = ($w, $h)")
        g.drawImage(image, x, y, x + w, y + h, 0, 0, iw, ih, null)
      }
    }
  }
  val imagePanel = new ImagePanel
  add(imagePanel, "grow, pushy, wrap")

  val elapsed = new Label(s"${TimePrinter.printDuration(0)}")
  val duration = new Label(s"${TimePrinter.printDuration(0)}")
  val slider = new PercentageSlider
  val controlPanel = new MigPanel("ins 0", "", "") {
    val progress = new MigPanel("ins 0", "", "") {
      add(elapsed, "pushy, wrap")
      add(duration, "pushy")
    }
    add(progress, "pushy")
    add(slider, "pushx, growx")
    add(new ImageButton("images/play.png", "Play", playOrPauseVideo()), "align right")
  }
  add(controlPanel, "growx")

  listenTo(slider)
  reactions += {
    case ValueChanged(`slider`) => player.foreach(_.seek(slider.percentage))
  }

  @volatile var player: Option[VideoPlayer] = None

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    this.telemetry = telemetry

    setup.videoPath.foreach{path =>
      player.foreach(_.close)
      player = Some(createPlayer(
        path, telemetry,
        (image: Image) => Swing.onEDT(imagePanel.show(image)),
        shiftHandler, controllerTimeUpdater
      ))
    }
  }

  private def controllerTimeUpdater(videoTs: Long, percentage: Double) {
    videoTimeUpdater(videoTs)
    Swing.onEDT{
      slider.percentage = percentage
      elapsed.text = s"${TimePrinter.printDuration(videoTs)}"
      duration.text = s"${TimePrinter.printDuration(player.map(_.duration))}"
    }
  }

  def playOrPauseVideo() {
    player.foreach(_.play())
  }
}
