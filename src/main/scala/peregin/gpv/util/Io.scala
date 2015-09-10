package peregin.gpv.util

import java.awt.image.BufferedImage
import java.io.Closeable
import javax.imageio.ImageIO
import javax.swing.{Icon, ImageIcon}


object Io extends Logging {

  lazy val emptyImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)

  def withCloseable[R](c: Closeable)(body: Closeable => R): R = try {
    body(c)
  } finally {
    c.close()
  }

  def getResource(path: String) = Io.getClass.getClassLoader.getResourceAsStream(path)

  def loadImage(path: String): BufferedImage = {
    val maybeResourceStream = Option(getResource(path))
    maybeResourceStream.map(ImageIO.read).getOrElse(emptyImage)
  }

  def loadIcon(path: String): Icon = new ImageIcon(loadImage(path))

  def copy(src: BufferedImage): BufferedImage = {
    val colorModel = src.getColorModel
    val raster = src.copyData(null)
    new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied, null)
  }

  def compare(im1: BufferedImage, im2: BufferedImage): Boolean = {
    (im1.getWidth, im2.getWidth, im1.getHeight, im2.getHeight) match {
      case (w1, w2, _, _) if w1 != w2 =>
        log.error(s"width mismatch $w1 <> $w2")
        false
      case (_, _, h1, h2) if h1 != h2 =>
        log.error(s"height mismatch $h1 <> $h2")
        false
      case (w1, w2, h1, h2) =>
        val coords = for (x <- 0 until w1; y <- 0 until h1) yield (x, y)
        coords.find{case (x: Int, y: Int) =>
          val p1 = im1.getRGB(x, y)
          val p2 = im2.getRGB(x, y)
          p1 != p2
        }.isEmpty
    }
  }
}
