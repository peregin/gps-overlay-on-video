package peregin.gpv.util

import java.awt.image.BufferedImage
import java.io.Closeable
import javax.imageio.ImageIO
import javax.swing.{Icon, ImageIcon}


object Io {

  def withCloseable[R](c: Closeable)(body: Closeable => R): R = try {
    body(c)
  } finally {
    c.close()
  }

  def getResource(path: String) = classOf[App].getClassLoader.getResourceAsStream(path)

  def loadImage(path: String): BufferedImage = ImageIO.read(getResource(path))

  def loadIcon(path: String): Icon = new ImageIcon(loadImage(path))

  def copy(src: BufferedImage): BufferedImage = {
    val colorModel = src.getColorModel
    val raster = src.copyData(null)
    new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied, null)
  }
}
