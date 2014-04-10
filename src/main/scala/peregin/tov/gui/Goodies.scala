package peregin.tov.gui

import scala.swing.{Label, Component, Dialog, Window}
import java.awt.{Point, Toolkit}
import org.jdesktop.swingx.JXBusyLabel


object Goodies {

  def center(w: Window) {
    val screen = Toolkit.getDefaultToolkit.getScreenSize
    val size = w.bounds
    val x = (screen.width - size.width) / 2
    val y = (screen.height - size.height) / 2
    w.location = new Point(x, y)
  }

  def lookBusy(w: Window)(body: => Unit) {
    val dlg = new Dialog(w) {
      modal = true
      peer.setUndecorated(true)
      contents = new MigPanel("ins 5", "", "") {
        val busy = new JXBusyLabel
        busy.setBusy(true)
        add(Component.wrap(busy), "")
        add(new Label("Loading..."), "")
      }
      import scala.concurrent._
      import ExecutionContext.Implicits.global
      future {
        body
        dispose()
      }
    }
    dlg.pack()
    Goodies.center(dlg)
    dlg.visible = true
  }
}
