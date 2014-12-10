package peregin.gpv.gui

import java.awt.{Color, Graphics, Image}
import javax.swing.JPanel


class ImagePanel extends JPanel {
  var image: Option[Image] = None
  var topImage: Option[Image] = None

  def show(im: Image) {
    show(im, None)
  }

  // imTop expected to have the same size as im
  // allows to paint a normal and top layer
  def show(im: Image, imTop: Option[Image]) {
    image = Some(im)
    topImage = imTop
    repaint()
  }

  override def paint(g: Graphics) = {
    val width = getWidth
    val height = getHeight
    g.setColor(Color.black)
    g.fillRect(0, 0, width, height)

    image.foreach { normalLayer =>
      val iw = normalLayer.getWidth(null)
      val ih = normalLayer.getHeight(null)

      // the image needs to be scaled to fit to the display area
      val (w, h) = if (iw > width || ih > height) {
        val scale = math.min(width.toDouble / iw, height.toDouble / ih)
        ((iw.toDouble * scale).toInt, (ih.toDouble * scale).toInt)
      } else (iw, ih)
      val x = (width - w) / 2
      val y = (height - h) / 2
      //debug(s"(w, h) = ($w, $h)")
      g.drawImage(normalLayer, x, y, x + w, y + h, 0, 0, iw, ih, null)

      topImage.foreach{topLayer =>
        g.drawImage(topLayer, x, y, x + w, y + h, 0, 0, iw, ih, null)
      }
    }
  }
}
