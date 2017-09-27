package peregin.gpv.gui.gauge

import java.awt._
import java.awt.geom.{Area, Ellipse2D, Rectangle2D}
import java.awt.image.BufferedImage

import org.apache.batik.transcoder.{SVGAbstractTranscoder, TranscoderInput, TranscoderOutput}
import org.apache.batik.transcoder.image.ImageTranscoder
import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.Io
import peregin.gpv.util.Trigo.{polarX, polarY}

trait SvgGauge extends GaugePainter {

  val heartStream = Io.getResource("images/heart.svg")
  val transcoderInput = new TranscoderInput(heartStream)
  val tc = new ImageTranscoder {
    private var img: BufferedImage = null;
    override def writeImage(img: BufferedImage, output: TranscoderOutput) = this.img = img
    override def createImage(width: Int, height: Int) = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    def getImage = img
  }


  lazy val dummy = InputValue(89, MinMax(62, 171))
  override def defaultInput = dummy
  override def sample(sonda: Sonda) {sonda.heartRate.foreach(input = _)}

  override def paint(g: Graphics2D, w: Int, h: Int) = {
    super.paint(g, w, h)

    tc.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, w.toFloat / 4)
    tc.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, h.toFloat / 4)
    tc.transcode(transcoderInput, null)

    val box = math.min(w, h)
    val cy = h / 2
    val d = box / 5
    val r = d / 4
    val ph = 4 * r

    // draw heart icon
    g.setColor(Color.white)
    val px = 10
    val py = (h - 10) / 2

    val svgImage = tc.getImage
    g.drawImage(svgImage, px, py, null)

    // scale down for the current level indicator
    val origWidth = svgImage.getWidth()
    val origHeight = svgImage.getHeight()
    val newWidth = origWidth - 5
    val newHeight = origHeight - 5
    val newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
    val gg = newImage.createGraphics()
    gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    gg.drawImage(svgImage, 0, 0, newWidth, newHeight, 0, 0, origWidth, origHeight, null)
    gg.dispose()
    for (x <- 0 until newWidth; y <- 0 until newHeight) {
      val rgb = newImage.getRGB(x, y)
      println(s"[$x,$y]=$rgb")
      if (rgb != 0) newImage.setRGB(x, y, 11184810 | (255 << 24))
    }
    g.drawImage(newImage, px, py, null)



    //g.fillOval(px, py, d / 2, d / 2)
    //g.fillOval(px + d / 2, py, d / 2, d / 2)
    val b = ph - r
    val oe = 2 * math.asin(r.toDouble / b) - math.Pi / 2
    val ty = polarY(py + ph, b, oe.toDegrees)
    val tx = polarX(px + 2 * r, b, oe.toDegrees)
    val romb = new Polygon
    romb.addPoint(px + 2 * r, py + ph)
    romb.addPoint(tx, ty)
    romb.addPoint(px + 2 * r, py + r)
    romb.addPoint(2 * (px + 2 * r) - tx, ty)
    val hr = new Area(romb)
    //hr.add(new Area(new Ellipse2D.Double(px, py, d / 2, d / 2)))
    hr.add(new Area(new Ellipse2D.Double(px + d / 2, py, d / 2, d / 2)))
    g.fill(hr)

    // fill the heart based on the current value
    val pointerHeight = (input.current - input.boundary.min) * ph / input.boundary.diff
    val a1 = new Area(new Rectangle2D.Double(px, py + ph - pointerHeight, d, ph))
    val a2 = new Area(romb)
    a2.add(new Area(new Ellipse2D.Double(px, py, d / 2, d / 2)))
    a2.add(new Area(new Ellipse2D.Double(px + d / 2, py, d / 2, d / 2)))
    a1.intersect(a2)
    g.setColor(Color.gray)
    g.fill(a1)

    // draw current value
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 4).toFloat))
    val text = f"${input.current}%2.0f"
    val tb = g.getFontMetrics.getStringBounds(text, g)
    textWidthShadow(g, text, px + (w - tb.getWidth) / 2, (h + tb.getHeight) / 2)
    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (box / 12).toFloat))
    val utext = "bpm"
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, px + (w - utb.getWidth) / 2, cy + utb.getHeight * 2.2)
  }

  import java.awt.image.IndexColorModel

    // a single shade of gray
  def createColorModel(n: Int): IndexColorModel = {
    val size = 16
    val r = Array.fill(size)(n.toByte)
    val g = Array.fill(size)(n.toByte)
    val b = Array.fill(size)(n.toByte)
    return new IndexColorModel(4, size, r, g, b)
  }
}
