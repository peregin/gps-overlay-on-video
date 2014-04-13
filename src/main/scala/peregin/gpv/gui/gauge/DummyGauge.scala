package peregin.gpv.gui.gauge

import peregin.gpv.gui.GaugeComponent
import java.awt.{Color, Font}
import scala.swing.{Graphics2D, Font}


class DummyGauge extends GaugeComponent {

   val sf = new Font("Dialog", Font.BOLD, 12)

   override def paint(g: Graphics2D) = {
     val w = peer.getWidth
     val h = peer.getHeight

     g.setColor(Color.yellow)
     g.drawRoundRect(1, 1, w - 2, h - 2, 5, 5)

     g.setColor(Color.white)
     g.setFont(sf)
     val text = "Speed Gauge - 18 km/h"
     val fm = g.getFontMetrics(sf)
     val sw = fm.stringWidth(text)
     g.drawString(text, (w - sw) / 2, (h + fm.getHeight) / 2)
   }
 }
