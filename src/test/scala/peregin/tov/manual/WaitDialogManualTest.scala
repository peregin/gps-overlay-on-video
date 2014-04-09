package peregin.tov.manual

import scala.swing._
import java.awt.Dimension
import peregin.tov.util.Logging
import scala.swing.event.ButtonClicked
import peregin.tov.App


object WaitDialogManualTest extends SimpleSwingApplication with Logging {

  override def top = frame
  val frame = new MainFrame {
    title = "hello"
    contents = new BoxPanel(Orientation.Vertical) {
      val testButton = new Button("Test")
      contents += testButton

      listenTo(testButton)
      reactions += {
        case ButtonClicked(`testButton`) => App.lookBusy{ Thread.sleep(2000) }
      }
    }
    size = new Dimension(1024, 768)
  }
}
