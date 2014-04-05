package peregin.tov

import scala.swing._
import java.awt.{Color, Point, Toolkit, Dimension}
import javax.imageio.ImageIO
import peregin.tov.gui.{DashboardPanel, VideoPanel, TelemetryPanel, MigPanel}
import javax.swing.{ImageIcon, Icon, JToolBar, UIManager}
import com.jgoodies.looks.plastic.{PlasticTheme, PlasticLookAndFeel, Plastic3DLookAndFeel}
import org.jdesktop.swingx.{JXButton, JXLabel, JXStatusBar, JXTitledPanel}
import peregin.tov.util.Logging
import java.awt.event.{ActionEvent, ActionListener}


object App extends SimpleSwingApplication with Logging {

  log.info("initializing...")

  initLookAndFeel()

  val frame = new MainFrame {
    // temporary - use this mock to have a proper layout
    class MockPanel(info: String) extends BoxPanel(Orientation.Vertical) {
      background = Color.blue
      contents += new Label(info) { foreground = Color.white }
    }

    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val toolbar = new JToolBar
      def createToolbarButton(image: String, tooltip: String, action: => Unit): JXButton = {
        val btn = new JXButton(loadIcon(image))
        btn.setToolTipText(tooltip)
        btn.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent) = action
        })
        btn
      }
      toolbar.add(createToolbarButton("images/new.png", "New", newProject))
      toolbar.add(createToolbarButton("images/open.png", "Open", openProject))
      toolbar.add(createToolbarButton("images/save.png", "Save", saveProject))
      toolbar.addSeparator()
      toolbar.add(createToolbarButton("images/play.png", "Export", exportProject))
      add(Component.wrap(toolbar), "span 2, wrap")

      val videoPanel = new VideoPanel
      add(titled("Video", videoPanel), "pushy, width 60%")
      val telemetryPanel = new TelemetryPanel
      add(titled("Telemetry Data", telemetryPanel), "pushy, width 40%, wrap")

      val dashboardPanel = new DashboardPanel
      add(titled("Dashboard", dashboardPanel), "height 30%, span 2, wrap")

      val statusPanel = new JXStatusBar
      statusPanel.add(new JXLabel("Ready"))
      add(Component.wrap(statusPanel), "span 2")
    }
  }

  frame.title = "Telemetry data on videos"
  frame.iconImage = loadImage("images/video.png")
  frame.size = new Dimension(1024, 768)
  center(frame)

  def top = frame

  def initLookAndFeel() {
    import PlasticLookAndFeel._
    import collection.JavaConverters._
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

  def loadImage(path: String): Image = ImageIO.read(classOf[App].getClassLoader.getResourceAsStream(path))
  def loadIcon(path: String): Icon = new ImageIcon(loadImage(path))

  def titled(title: String, c: Component): Component = {
    val panel = new JXTitledPanel(title, c.peer)
    Component.wrap(panel)
  }
  
  def newProject() {log.info("new project")}
  def openProject() {log.info("open project")}
  def saveProject() {log.info("save project")}
  def exportProject() {log.info("export project")}
}
