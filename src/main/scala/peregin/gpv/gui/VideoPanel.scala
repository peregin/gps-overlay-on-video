package peregin.gpv.gui

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import com.xuggle.mediatool.{IMediaReader, MediaToolAdapter, ToolFactory}
import java.awt.image.BufferedImage
import com.xuggle.mediatool.event.IVideoPictureEvent
import javax.swing.{JSlider, JPanel}
import java.awt.{Color, Graphics, Image}
import peregin.gpv.Setup
import peregin.gpv.util.Logging
import scala.swing.Swing
import scala.concurrent._


class VideoPanel(openVideoData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging {

  val chooser = new FileChooserPanel("Load video file:", openVideoData, new FileNameExtensionFilter("Video files (mp4)", "mp4"))
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

  val controlPanel = new MigPanel("ins 0", "", "") {
    add(new JSlider(0, 100, 0), "pushx, growx")
    add(new ImageButton("images/play.png", "Play", playOrPauseVideo), "align right")
  }
  add(controlPanel, "growx")

  @volatile var reader: Option[IMediaReader] = None

  def refresh(setup: Setup) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    setup.videoPath.foreach{path =>
      synchronized {
        reader.foreach {
          mr =>
            if (mr.isOpen) mr.close()
        }
        reader = Some(ToolFactory.makeReader(path))
        reader.foreach{mr =>
          mr.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
          mr.addListener(new MediaToolAdapter {
            override def onVideoPicture(event: IVideoPictureEvent) = {
              log.debug(s"ts = ${event.getTimeStamp} ${event.getTimeUnit}")
              Swing.onEDT(imagePanel.show(event.getImage))
              super.onVideoPicture(event)
            }
          })
        import ExecutionContext.Implicits.global
          future {
            (0 until 200).foreach{_ =>
              mr.readPacket()
              Thread.sleep(30)
            }
          }
        }
      }
    }
  }

  def playOrPauseVideo() {
    log.info("play video...")
  }
}
