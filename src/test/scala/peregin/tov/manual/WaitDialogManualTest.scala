package peregin.tov.manual

import scala.swing._
import java.awt.Dimension
import peregin.tov.util.{Logging, Align}
import org.jdesktop.swingx.JXBusyLabel
import peregin.tov.gui.MigPanel
import scala.swing.event.ButtonClicked


object WaitDialogManualTest extends SimpleSwingApplication with Logging {

  override def top = frame
  val frame = new MainFrame {
    title = "hello"
    contents = new BoxPanel(Orientation.Vertical) {
      val testButton = new Button("Test")
      contents += testButton

      listenTo(testButton)
      reactions += {
        case ButtonClicked(`testButton`) => testSomething
      }
    }
    size = new Dimension(1024, 768)
  }

  def testSomething {
    val dlg = new Dialog(frame) {
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
        log.info("sleeping...")
        Thread.sleep(2000)
        log.info("woke up...")
        dispose()
      }
    }
    dlg.pack()
    Align.center(dlg)
    dlg.visible = true
  }
}
