package peregin.gpv.gui.gauge

import java.awt.Font
import peregin.gpv.util.Io


trait DigitalFont {

  lazy val digitalFont = Font.createFont(Font.TRUETYPE_FONT, Io.getResource("fonts/digital-7.ttf"))
}
