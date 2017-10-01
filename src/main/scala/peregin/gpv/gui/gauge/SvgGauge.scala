package peregin.gpv.gui.gauge

import java.awt._

import peregin.gpv.util.ImageConverter

trait SvgGauge extends GaugePainter {

  def imagePath: String

  def valueText: String
  def unitText: String

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

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
    val svgImage = ImageConverter.loadSvg(imagePath, sx, sy)
    val whiteImage = ImageConverter.fillColor(svgImage, 255, 255, 255)
    g.drawImage(whiteImage, px, py, null)

    // fill the heart based on the current value
    val pointerHeight = (input.current - input.boundary.min) * sy / input.boundary.diff
    g.clipRect(px, py + sy - pointerHeight.toInt, sx, pointerHeight.toInt)
    val grayImage = ImageConverter.fillColor(svgImage, 150, 150, 150)
    g.drawImage(grayImage, px, py, null)
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
