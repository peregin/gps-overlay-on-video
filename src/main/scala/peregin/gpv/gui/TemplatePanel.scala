package peregin.gpv.gui

import java.awt.{Color, Component, Font, Graphics, Graphics2D}

import javax.swing._
import javax.swing.event.ListSelectionEvent
import org.jdesktop.swingx.JXList
import peregin.gpv.gui.TemplatePanel.{Listener, TemplateEntry}
import peregin.gpv.gui.dashboard.{CyclingDashboard, Dashboard, MotorBikingDashboard, SkiingDashboard}
import peregin.gpv.model.Sonda
import peregin.gpv.util.Io

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

  val templates = new JXList(model)
  templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  templates.setSelectedIndex(0)
  templates.setFont(new Font("Arial", Font.BOLD, 18))
  templates.setCellRenderer(new TemplateCellRenderer)

  add(templates, "grow, push")

  templates.addListSelectionListener((e: ListSelectionEvent) => {
    if (!e.getValueIsAdjusting) {
      templates.getSelectedValue match {
        case entry: TemplateEntry =>
          listener.selected(entry)
          preview.repaint()
        case _ =>
      }
    }
  })

  // shows the current selection
  val preview = new JPanel() {

    override def paint(g: Graphics): Unit = {
      val width = getWidth
      val height = getHeight
      g.setColor(Color.black)
      g.fillRect(0, 0, width, height)

      templates.getSelectedValue match {
        case entry: TemplateEntry =>
          g.translate(0, height / 2)
          entry.dashboard.paintDashboard(g.asInstanceOf[Graphics2D], width, width / 6, Sonda.sample)
        case _ =>
      }
    }
  }

  add(preview, "grow, push")
}
