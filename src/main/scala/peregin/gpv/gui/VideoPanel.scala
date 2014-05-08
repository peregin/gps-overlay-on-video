package peregin.gpv.gui

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import com.xuggle.mediatool.{IMediaReader, ToolFactory}
import java.awt.image.BufferedImage
import javax.swing.{JSlider, JPanel}
import java.awt.{Color, Graphics, Image}
import peregin.gpv.Setup
import peregin.gpv.util.Logging
import scala.swing.Swing
import scala.concurrent._
import peregin.gpv.model.Telemetry
import peregin.gpv.gui.video.{VideoController, VideoOverlay}


class VideoPanel(openVideoHandler: File => Unit, timeHandler: Long => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging {

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
      g.setColor(Color.black)
      val width = getWidth
      val height = getHeight
      g.fillRect(0, 0, width, height)

      if (image != null) {
        val w = image.getWidth(null)
        val h = image.getHeight(null)
        val x = (width - w) / 2
        val y = (height - h) / 2
        g.drawImage(image, x, y, w, h, null)
      }
    }
  }
  val imagePanel = new ImagePanel
  add(imagePanel, "grow, pushy, wrap")

  val slider = new JSlider(0, 100, 0)
  val controlPanel = new MigPanel("ins 0", "", "") {
    add(slider, "pushx, growx")
    add(new ImageButton("images/play.png", "Play", playOrPauseVideo()), "align right")
  }
  add(controlPanel, "growx")

  @volatile var reader: Option[IMediaReader] = None

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    this.telemetry = telemetry

    setup.videoPath.foreach{path =>
      reader.foreach {
        mr => if (mr.isOpen) mr.close()
      }
      reader = Some(ToolFactory.makeReader(path))
      reader.foreach{ mr =>
        mr.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)

        mr.addListener(new VideoOverlay(telemetry, (image: Image) => Swing.onEDT(imagePanel.show(image))))

        val controller = new VideoController(timeHandler)
        mr.addListener(controller)

        import ExecutionContext.Implicits.global
        future {
          while(mr.readPacket() == null) {
            // runs in a loop until the end
          }
        }
      }
    }
  }

  def playOrPauseVideo() {
    log.info("play video...")
  }
}
