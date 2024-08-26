package peregin.gpv.gui

import peregin.gpv.util.ImageConverter

import java.awt.geom.AffineTransform
import java.awt.{Color, Graphics, Graphics2D, Image}
import javax.swing.JPanel


class ImagePanel extends JPanel {
  var image: Option[Image] = None
  var topImage: Option[Image] = None
  var rotation: Double = 0.0

  def show(im: Image, rotation: Double): Unit = {
    show(im, None, rotation)
  }

  // imTop expected to have the same size as im
  // allows to paint a normal and top layer
  def show(im: Image, imTop: Option[Image], rotation: Double): Unit = {
    image = Some(im)
    topImage = imTop
    this.rotation = rotation
    repaint()
  }

  override def paint(g: Graphics): Unit = {
    val width = getWidth
    val height = getHeight
    g.setColor(Color.black)
    g.fillRect(0, 0, width, height)

    image.foreach { normalLayer =>
      val rotated = ImageConverter.rotateImage(normalLayer, rotation)
      val iw = rotated.getWidth(null)
      val ih = rotated.getHeight(null)

      // the image needs to be scaled to fit to the display area
      val (w, h) = if (iw > width || ih > height) {
        val scale = math.min(width.toDouble / iw, height.toDouble / ih)
        ((iw.toDouble * scale).toInt, (ih.toDouble * scale).toInt)
      } else (iw, ih)
      val x = (width - w) / 2
      val y = (height - h) / 2
      //debug(s"(w, h) = ($w, $h)")
      g.drawImage(rotated, x, y, x + w, y + h, 0, 0, iw, ih, null)

      topImage.foreach { topLayer =>
        g.drawImage(ImageConverter.rotateImage(topLayer, rotation), x, y, x + w, y + h, 0, 0, iw, ih, null)
      }
    }
  }
}
