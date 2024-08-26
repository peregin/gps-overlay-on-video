package peregin.gpv.util

import java.awt.image.{BufferedImage, IndexColorModel}
import org.apache.batik.transcoder.image.ImageTranscoder
import org.apache.batik.transcoder.{SVGAbstractTranscoder, TranscoderInput, TranscoderOutput}

import java.awt.Image

object ImageConverter {

  def loadSvg(imagePath: String, w: Int, h: Int): BufferedImage = {
    val heartStream = Io.getResource(imagePath)
    val transcoderInput = new TranscoderInput(heartStream)
    val tc = new ImageTranscoder {
      private var img: BufferedImage = null;

      override def writeImage(img: BufferedImage, output: TranscoderOutput) = this.img = img

      override def createImage(width: Int, height: Int) = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

      def getImage = img
    }

    tc.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, w.toFloat)
    tc.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, h.toFloat)
    tc.transcode(transcoderInput, null)

    tc.getImage
  }

  def fillColor(im: BufferedImage, r: Int, g: Int, b: Int): BufferedImage = {
    val newWidth = im.getWidth
    val newHeight = im.getHeight
    for (x <- 0 until newWidth; y <- 0 until newHeight) {
      val rgb = im.getRGB(x, y)
      val a = alpha(rgb)
      //println(s"[$x,$y]=$rgb")
      if (rgb != 0) im.setRGB(x, y, argb(a, r, g, b))
    }
    im
  }


  def argb(a: Int, r: Int, g: Int, b: Int) = {
    ((a & 0xFF) << 24) |
      ((r & 0xFF) << 16) |
      ((g & 0xFF) << 8)  |
      ((b & 0xFF) << 0)
  }

  def alpha(color: Int): Int = (color >> 24) & 0xFF

  // a single shade of gray
  def createColorModel(n: Int): IndexColorModel = {
    val size = 16
    val r = Array.fill(size)(n.toByte)
    val g = Array.fill(size)(n.toByte)
    val b = Array.fill(size)(n.toByte)
    new IndexColorModel(4, size, r, g, b)
  }

  def rotateImage(image: Image, rotation: Double): Image = {
    if (rotation == 0) {
      return image
    }
    val result = new BufferedImage(
      if (Math.abs(rotation) == 180) image.getWidth(null) else image.getHeight(null),
      if (Math.abs(rotation) == 180) image.getHeight(null) else image.getWidth(null),
      BufferedImage.TYPE_INT_ARGB
    )
    val g = result.createGraphics()
    g.rotate(Math.toRadians(-rotation), result.getWidth(null) / 2, result.getHeight(null) / 2)
    g.translate((result.getWidth(null) - image.getWidth(null)) / 2, (result.getHeight(null) - image.getHeight(null)) / 2)
    g.drawImage(image, 0, 0, null)
    g.dispose()
    result
  }
}
