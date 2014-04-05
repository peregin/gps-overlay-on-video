package peregin.tov

import scala.swing._
import java.awt.{Color, Point, Toolkit, Dimension}
import javax.imageio.ImageIO
import peregin.tov.gui.MigPanel


object App extends SimpleSwingApplication {

  val frame = new MainFrame {
    title = "Telemetry data overlay on videos"

    class MockPanel(info: String) extends BoxPanel(Orientation.Vertical) {
      background = Color.blue
      contents += new Label(info) { foreground = Color.white }
    }
    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val projectPanel = new MockPanel("project setup: new, open, save, export")
      add(projectPanel, "span 2, wrap")

      val videoPanel = new MockPanel("video")
      add(videoPanel, "pushy")
      val telemetryPanel = new MockPanel("telemetry")
      add(telemetryPanel, "pushy, wrap")

      val dashboardPanel = new MockPanel("widgets and templates")
      add(dashboardPanel, "height 30%, span 2, wrap")

      val statusPanel = new MockPanel("ready")
      add(statusPanel, "span 2")
    }

    size = new Dimension(1024, 768)
    iconImage = loadImage("images/video.png")
  }
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
}
