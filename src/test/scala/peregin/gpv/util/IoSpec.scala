package peregin.gpv.util

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import org.specs2.mutable.Specification

/**
 * Created by peregin on 02/12/14.
 */
class IoSpec extends Specification {

  "copy image" in {
    val src = Io.loadImage("images/distance.png")
    val dst = Io.copy(src)
    val barr = new ByteArrayOutputStream()
    ImageIO.write(dst, "png", barr)
    barr.size() === 3676
  }
}
