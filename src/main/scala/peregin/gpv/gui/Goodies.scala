package peregin.gpv.gui

import org.jdesktop.swingx.error.ErrorInfo

import scala.swing.{Component, Label, Dialog, Window}
import java.awt.{Point, Toolkit}
import org.jdesktop.swingx.{JXErrorPane, JXBusyLabel}
import com.jgoodies.looks.plastic.{Plastic3DLookAndFeel, PlasticTheme, PlasticLookAndFeel}
import javax.swing.UIManager

import scala.util.control.NonFatal


object Goodies {

  // on Mac start with VM parameter -Xdock:name="GSPonVideo"
  def initLookAndFeel() {
    import PlasticLookAndFeel._
    import collection.JavaConverters._
    sys.props += "apple.laf.useScreenMenuBar" -> "true"
    sys.props += "com.apple.mrj.application.apple.menu.about.name" -> "GPSonVideo"
    val theme = getInstalledThemes.asScala.map(_.asInstanceOf[PlasticTheme]).find(_.getName == "Dark Star")
    theme.foreach(setPlasticTheme)
    UIManager.setLookAndFeel(new Plastic3DLookAndFeel())
  }

  def center(w: Window) {
    val screen = Toolkit.getDefaultToolkit.getScreenSize
    val size = w.bounds
    val x = (screen.width - size.width) / 2
    val y = (screen.height - size.height) / 2
    w.location = new Point(x, y)
  }

  def showBusy(w: Window)(body: => Unit) {
    val dlg = new Dialog(w) {
      modal = true
      peer.setUndecorated(true)
      contents = new MigPanel("ins 5", "", "") {
        val busy = new JXBusyLabel
        busy.setBusy(true)
        add(busy, "")
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

  def showPopupOnFailure(w: Window)(body: => Unit) = {
    try {body}
    catch {case NonFatal(any) =>
      val c: Component = Component.wrap(w.peer.getRootPane)
      Dialog.showMessage(c, any.getMessage, "Error", Dialog.Message.Error)
    }
  }
}
