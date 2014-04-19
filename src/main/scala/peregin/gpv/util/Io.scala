package peregin.gpv.util

import java.io.Closeable
import scala.swing._
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, Icon}


object Io {

  def withCloseable[R](c: Closeable)(body: Closeable => R): R = try {
    body(c)
  } finally {
    c.close()
  }

  def loadImage(path: String): Image = ImageIO.read(classOf[App].getClassLoader.getResourceAsStream(path))
  def loadIcon(path: String): Icon = new ImageIcon(loadImage(path))
}
