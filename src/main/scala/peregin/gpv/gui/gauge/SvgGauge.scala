package peregin.gpv.gui.gauge

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import peregin.gpv.util.ImageConverter

import java.awt._
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

object ImageCache {

  case class CacheKey(imagePath: String, w: Int, h: Int, gray: Int)

  val cache: LoadingCache[CacheKey, BufferedImage] = CacheBuilder
    .newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(1, TimeUnit.DAYS)
    .build(new CacheLoader[CacheKey, BufferedImage] {
      override def load(key: CacheKey): BufferedImage = {
        val svgImage = ImageConverter.loadSvg(key.imagePath, key.w, key.h)
        ImageConverter.fillColor(svgImage, key.gray, key.gray, key.gray)
      }
    })

  def svgImage(imagePath: String, w: Int, h: Int, gray: Int): BufferedImage = {
    val key = CacheKey(imagePath, w, h, gray)
    cache.get(key)
  }
}

trait SvgGauge extends GaugePainter {

  def imagePath: String

  def valueText: String

  def unitText: String

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int) = {
    super.paint(g, devHeight, w, h)

    val box = math.min(w, h)
    val cy = h / 2
    val d = box / 5
    val r = d / 4

    // draw heart icon
    g.setColor(Color.white)
    val px = 10
    val py = (h - 10) / 2

    val sx = w / 4
    val sy = h / 4

    val whiteImage = ImageCache.svgImage(imagePath, sx, sy, 255)
    g.drawImage(whiteImage, px, py, null)

    // fill the heart based on the current value
    if (input.current.isDefined) {
      val pointerHeight = (input.current.get - input.boundary.min) * sy / input.boundary.diff
      g.clipRect(px, py + sy - pointerHeight.toInt, sx, pointerHeight.toInt)
    }

    val grayImage = ImageCache.svgImage(imagePath, sx, sy, 150)
    g.drawImage(grayImage, px, py, null)
    // revert clip
    g.setClip(null)

    // draw current value
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 4).toFloat))
    val tb = g.getFontMetrics.getStringBounds(valueText, g)
    textWidthShadow(g, valueText, px + (w - tb.getWidth) / 2, (h + tb.getHeight) / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 12).toFloat))
    val utb = g.getFontMetrics.getStringBounds(unitText, g)
    textWidthShadow(g, unitText, px + (w - utb.getWidth) / 2, cy + utb.getHeight * 2.2)
  }

}
