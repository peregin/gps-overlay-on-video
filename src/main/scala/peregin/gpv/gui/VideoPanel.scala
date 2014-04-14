package peregin.gpv.gui

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import com.xuggle.mediatool.{MediaToolAdapter, ToolFactory}
import java.awt.image.BufferedImage
import com.xuggle.mediatool.event.IVideoPictureEvent
import javax.swing.JPanel
import java.awt.{Color, Graphics, Image}
import peregin.gpv.Setup


class VideoPanel(openVideoData: File => Unit) extends MigPanel("ins 2", "", "[fill]") {

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

  def refresh(setup: Setup) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    setup.videoPath.foreach{path =>
      val mediaReader = ToolFactory.makeReader(path)
      mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
      mediaReader.addListener(new MediaToolAdapter {
        override def onVideoPicture(event: IVideoPictureEvent) = {

          imagePanel.show(event.getImage)

          super.onVideoPicture(event)
        }
      })
      (1 until 100)foreach(_ => mediaReader.readPacket())
    }
  }
}
