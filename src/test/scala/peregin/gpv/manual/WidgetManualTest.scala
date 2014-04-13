package peregin.gpv.manual

import scala.swing._
import java.awt.Dimension
import peregin.gpv.util.Logging
import scala.swing.event.ButtonClicked
import peregin.gpv.gui.Goodies


object WidgetManualTest extends SimpleSwingApplication with Logging {

  override def top = frame
  val frame = new MainFrame {
    title = "hello"
    contents = new BoxPanel(Orientation.Vertical) {
      val waitButton = new Button("Test Wait Modal")
      contents += waitButton

      val testButton = new Button("Some Test")
      contents += testButton

      listenTo(waitButton, testButton)
      reactions += {
        case ButtonClicked(`waitButton`) => test()
        case ButtonClicked(`testButton`) => someTest()
      }
    }
    size = new Dimension(1024, 768)
  }

  def test() {
    Goodies.showBusy(frame){ Thread.sleep(2000) }
  }

  def someTest() {
    println("hello")
  }
}
