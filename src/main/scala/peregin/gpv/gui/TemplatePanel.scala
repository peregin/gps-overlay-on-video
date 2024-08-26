package peregin.gpv.gui

import java.awt.{Color, Component, Font, Graphics, Graphics2D}
import javax.swing._
import javax.swing.event.ListSelectionEvent
import org.jdesktop.swingx.JXList
import peregin.gpv.Setup
import peregin.gpv.gui.TemplatePanel.{Listener, TemplateEntry}
import peregin.gpv.gui.dashboard.{CyclingDashboard, Dashboard, MotorBikingDashboard, SailingDashboard, SkiingDashboard, YamlResourceDashboardLoader}
import peregin.gpv.gui.gauge.{ChartPainter, ElevationChart}
import peregin.gpv.model.{InputValue, MinMax, Sonda, Telemetry}
import peregin.gpv.util.Io

import scala.jdk.CollectionConverters._

object TemplatePanel {

  case class TemplateEntry(name: String, dashboard: Dashboard) {
    override def toString: String = name
  }

  trait Listener {
    def selected(entry: TemplateEntry): Unit
  }

}

// save/load/use dashboard templates (set of already selected and aligned gauges)
class TemplatePanel(listener: Listener) extends MigPanel("ins 2", "[fill]", "[fill]") {

  class TemplateCellRenderer extends JLabel with ListCellRenderer[TemplateEntry] {

    val anIcon: Icon = Io.loadIcon("images/video.png")

    setOpaque(true)

    override def getListCellRendererComponent(list: JList[_ <: TemplateEntry], value: TemplateEntry, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component = {

      if (isSelected) {
        setBackground(list.getSelectionBackground)
        setForeground(list.getSelectionForeground)
      } else {
        setBackground(list.getBackground)
        setForeground(list.getForeground)
      }

      setFont(list.getFont)
      setText(value.toString)

      setIcon(anIcon)
      this
    }
  }

  val model = new DefaultListModel[TemplateEntry]
  model.addElement(TemplateEntry("Cycling", new CyclingDashboard {}))
  model.addElement(TemplateEntry("Skiing", new SkiingDashboard {}))
  model.addElement(TemplateEntry("MotorBiking", new MotorBikingDashboard {}))
  model.addElement(TemplateEntry("Sailing", new SailingDashboard {}))
  model.addElement(TemplateEntry("Complex Cycling", YamlResourceDashboardLoader.loadCpDashboard(classOf[Dashboard], "CyclingComplexDashboard.yaml")))

  val templates = new JXList(model)
  templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  templates.setSelectedIndex(0)
  templates.setFont(new Font("Arial", Font.BOLD, 18))
  templates.setCellRenderer(new TemplateCellRenderer)

  add(templates, "grow, push")

  def getSelectedEntry: Option[TemplateEntry] = templates.getSelectedValue match {
    case entry: TemplateEntry => Some(entry)
    case _ => None
  }

  templates.addListSelectionListener((e: ListSelectionEvent) => {
    if (!e.getValueIsAdjusting) {
      getSelectedEntry.foreach { entry =>
        listener.selected(entry)
        preview.repaint()
      }
    }
  })

  // shows the current selection
  val preview = new JPanel() {

    // clone painters
    private val name2Dashboard = model.elements().asScala.map {
      entry =>
        val dashboard = entry.dashboard.clone()
        // setup with default values
        dashboard.gauges().foreach {
          case e: ElevationChart => e.telemetry = Telemetry.sample()
          case _ =>
        }
        (entry.name, dashboard)
    }.toMap

    private lazy val motorBikeSample = Sonda.sample().copy(speed = InputValue(181, MinMax.max(230)))
    private lazy val regularSample = Sonda.sample()

    override def paint(g: Graphics): Unit = {
      val width = getWidth
      val height = getHeight
      g.setColor(Color.black)
      g.fillRect(0, 0, width, height)

      getSelectedEntry.flatMap(e => name2Dashboard.get(e.name)).foreach { d =>
        val sample = if (d.isInstanceOf[MotorBikingDashboard]) motorBikeSample else regularSample
        d.paintDashboard(g.asInstanceOf[Graphics2D], width, height, width / 5, sample)
      }
    }
  }

  add(preview, "grow, push")

  def refresh(setup: Setup): Unit = {
    if (setup.dashboardCode.isDefined) {
      for (i <- 0 until model.size()) {
        if (model.get(i).name.equals(setup.dashboardCode.get)) {
          templates.setSelectedIndex(i)
          return
        }
      }
    }
  }
}
