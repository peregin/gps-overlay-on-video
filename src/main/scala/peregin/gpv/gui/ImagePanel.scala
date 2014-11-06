package peregin.gpv.gui

import java.awt.{Color, Graphics, Image}
import javax.swing.JPanel


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
