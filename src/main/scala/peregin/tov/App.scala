package peregin.tov

import scala.swing._
import java.awt.{Color, Point, Toolkit, Dimension}
import javax.imageio.ImageIO
import peregin.tov.gui.MigPanel
import javax.swing.UIManager
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel
import org.jdesktop.swingx.JXTitledPanel
import peregin.tov.util.Logging


object App extends SimpleSwingApplication with Logging {

  log.info("initializing...")

  UIManager.setLookAndFeel(new Plastic3DLookAndFeel())

  val frame = new MainFrame {
    class MockPanel(info: String) extends BoxPanel(Orientation.Vertical) {
      background = Color.blue
      contents += new Label(info) { foreground = Color.white }
    }
    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val projectPanel = new MockPanel("project setup: new, open, save, export")
      add(projectPanel, "span 2, wrap")

      val videoPanel = new MockPanel("video")
      add(titled("Video", videoPanel), "pushy, width 60%")
      val telemetryPanel = new MockPanel("telemetry")
      add(titled("Telemetry Data", telemetryPanel), "pushy, width 40%, wrap")

      val dashboardPanel = new MockPanel("widgets and templates")
      add(dashboardPanel, "height 30%, span 2, wrap")

      val statusPanel = new MockPanel("ready")
      add(statusPanel, "span 2")
    }
  }

  frame.title = "Telemetry data on videos"
  frame.iconImage = loadImage("images/video.png")
  frame.size = new Dimension(1024, 768)
  center(frame)

  def top = frame

  def center(w: Window) {
    val screen = Toolkit.getDefaultToolkit.getScreenSize
    val size = w.bounds
    val x = (screen.width - size.width) / 2
    val y = (screen.height - size.height) / 2
    w.location = new Point(x, y)
  }

  def loadImage(path: String) = ImageIO.read(classOf[App].getClassLoader.getResourceAsStream(path))

  def titled(title: String, c: Component): Component = {
    val panel = new JXTitledPanel(title, c.peer)
    Component.wrap(panel)
  }
}
