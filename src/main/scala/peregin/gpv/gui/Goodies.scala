package peregin.gpv.gui

import java.awt.event.{ActionEvent, KeyEvent}
import java.awt.{Point, Toolkit}
import javax.swing.{KeyStroke, JComponent, UIManager}

import com.jgoodies.looks.plastic.{Plastic3DLookAndFeel, PlasticLookAndFeel, PlasticTheme}
import org.jdesktop.swingx.JXBusyLabel

import scala.swing.{Component, Dialog, Label, Window}
import scala.util.control.NonFatal


object Goodies {

  // on Mac start with VM parameter -Xdock:name="GSPonVideo"
  def initLookAndFeel() {
    import com.jgoodies.looks.plastic.PlasticLookAndFeel._

import scala.collection.JavaConverters._
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
      import scala.concurrent.ExecutionContext.Implicits.global
      import scala.concurrent._
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

  def mapEscapeTo(dialog: Dialog, cancelFunc: () => Unit) {
    dialog.peer.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelDialog")
    dialog.peer.getRootPane().getActionMap().put("cancelDialog", new javax.swing.AbstractAction() {
      def actionPerformed(e: ActionEvent) = cancelFunc()
    })
  }
}
