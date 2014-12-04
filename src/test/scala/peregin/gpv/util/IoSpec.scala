package peregin.gpv.util

import javax.imageio.ImageIO

import org.specs2.mutable.Specification

/**
 * Created by peregin on 02/12/14.
 */
class IoSpec extends Specification {

  "copy image" in {
    val src = loadImage("images/distance.png")
    val dst = Io.copy(src)
    Io.compare(src, dst) must beTrue
  }

  "compare different images" in {
    val im1 = loadImage("images/distance.png")
    val im2 = loadImage("images/time.png")
    Io.compare(im1, im2) must beFalse
  }

  private def loadImage(name: String) = {
    val is = getClass.getClassLoader.getResourceAsStream(name)
    ImageIO.read(is)
  }
}
