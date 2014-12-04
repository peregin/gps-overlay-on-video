package peregin.gpv.util

import org.specs2.mutable.Specification

/**
 * Created by peregin on 02/12/14.
 */
class IoSpec extends Specification {

  "copy image" in {
    val src = Io.loadImage("images/distance.png")
    val dst = Io.copy(src)
    Io.compare(src, dst) must beTrue
  }

  "compare different images" in {
    val im1 = Io.loadImage("images/distance.png")
    val im2 = Io.loadImage("images/time.png")
    Io.compare(im1, im2) must beFalse
  }
}
