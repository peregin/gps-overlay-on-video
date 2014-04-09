package peregin.tov.util

import scala.swing.Window
import java.awt.{Point, Toolkit}


object Align {

  def center(w: Window) {
    val screen = Toolkit.getDefaultToolkit.getScreenSize
    val size = w.bounds
    val x = (screen.width - size.width) / 2
    val y = (screen.height - size.height) / 2
    w.location = new Point(x, y)
  }
}
