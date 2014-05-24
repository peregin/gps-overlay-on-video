package peregin.gpv.gui

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.{JSlider, JPanel}
import java.awt.{Color, Graphics, Image}
import peregin.gpv.Setup
import peregin.gpv.util.Logging
import scala.swing.Swing
import peregin.gpv.model.Telemetry
import peregin.gpv.gui.video.VideoPlayer
import javax.swing.event.{ChangeEvent, ChangeListener}


class VideoPanel(openVideoHandler: File => Unit, videoTimeUpdater: Long => Unit, shiftHandler: => Long) extends MigPanel("ins 2", "", "[fill]") with Logging {

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

  val slider = new JSlider(0, 100, 0)
  var sliderChangeFromApi = true
  slider.setPaintTrack(true)
  slider.setPaintTicks(true)
  slider.setMajorTickSpacing(10)
  slider.setMinorTickSpacing(1)
  slider.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = {
      if (!slider.getValueIsAdjusting && !sliderChangeFromApi) {
        debug(s"slider ${slider.getValue} event")
        player.foreach(_.seek(slider.getValue))
      }
    }
  })
  val controlPanel = new MigPanel("ins 0", "", "") {
    add(slider, "pushx, growx")
    add(new ImageButton("images/play.png", "Play", playOrPauseVideo()), "align right")
  }
  add(controlPanel, "growx")

  @volatile var player: Option[VideoPlayer] = None

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    this.telemetry = telemetry

    setup.videoPath.foreach{path =>
      player.foreach(_.close)
      player = Some(new VideoPlayer(path, telemetry,
        (image: Image) => Swing.onEDT(imagePanel.show(image)), shiftHandler, controllerTimeUpdater))
    }
  }

  private def controllerTimeUpdater(videoTs: Long, percentage: Int) {
    videoTimeUpdater(videoTs)
    Swing.onEDT{
      sliderChangeFromApi = true
      slider.setValue(percentage)
      sliderChangeFromApi = false
    }
  }

  def playOrPauseVideo() {
    info("play video...")
  }
}
