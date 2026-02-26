package peregin.gpv.gui.gauge

import java.awt.{Color, Font, Graphics2D}

trait SvgDigitalGauge extends SvgGauge with DigitalFont {

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int) = {

    val box = math.min(w, h)
    val cy = h / 2
    val d = box / 5

    // draw heart icon
    g.setColor(Color.white)

    val dx = 20

    val px = dx
    val py = h/3

    val sx = w/3
    val sy = h/3

    val blackOutlineOfSvgImage = 3

    val blackImage = ImageCache.svgImage(imagePath, sx + 2 * blackOutlineOfSvgImage, sy + 2 * blackOutlineOfSvgImage, 0)
    g.drawImage(blackImage, px - blackOutlineOfSvgImage, py - blackOutlineOfSvgImage, null)

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

    val fs = box.toFloat / 2

    // draw current speed
    g.setFont(digitalFont.deriveFont(Font.BOLD, fs))
    val text = valueText
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, dx + (w - tb.getWidth) / 2, cy + box / 2 - tb.getHeight * 1.2)

    // draw unit
    g.setFont(digitalFont.deriveFont(Font.BOLD, fs / 4))
    val utext = unitText
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, dx +  (w - utb.getWidth) / 2, cy + box / 2 + utb.getHeight * 2 - tb.getHeight * 1.2)
  }



}
