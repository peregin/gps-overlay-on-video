package peregin.gpv.gui

import java.awt.{Component, Font}
import javax.swing._
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}

import org.jdesktop.swingx.JXList
import peregin.gpv.util.{Io, Logging}

// save/load/use dashboard templates (set of already selected and aligned gauges)
class TemplatePanel extends MigPanel("ins 2", "[fill]", "[fill]") with Logging {

  case class TemplateEntry(name: String) {
    override def toString: String = name
  }

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
  model.addElement(TemplateEntry("Cycling"))
  model.addElement(TemplateEntry("Skiing"))
  model.addElement(TemplateEntry("MotorBiking"))

  val templates = new JXList(model)
  templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  templates.setSelectedIndex(0)
  templates.setFont(new Font("Arial", Font.BOLD, 18))
  templates.setCellRenderer(new TemplateCellRenderer)

  add(templates, "grow, push")

  templates.addListSelectionListener((e: ListSelectionEvent) => {
    if (!e.getValueIsAdjusting) {
      val selected = templates.getSelectedValue
      log.info(s"selected $selected")
    }
  })
}
