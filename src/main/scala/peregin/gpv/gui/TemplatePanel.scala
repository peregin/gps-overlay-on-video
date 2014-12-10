package peregin.gpv.gui

import java.awt.{Component, Font}
import javax.swing._

import org.jdesktop.swingx.JXList
import peregin.gpv.util.Io

// save/load/use templates (set of already selected and aligned gauges)
class TemplatePanel extends MigPanel("ins 2", "[fill]", "[fill]") {

  case class TemplateEntry(name: String) {
    override def toString = name
  }

  class TemplateCellRenderer extends JLabel with ListCellRenderer[TemplateEntry] {

    val anIcon = Io.loadIcon("images/video.png")

    setOpaque(true)

    override def getListCellRendererComponent(list: JList[_ <: TemplateEntry], value: TemplateEntry, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component = {

      if (isSelected) {
        setBackground(list.getSelectionBackground())
        setForeground(list.getSelectionForeground())
      } else {
        setBackground(list.getBackground())
        setForeground(list.getForeground())
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

  val templates = new JXList(model)
  templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  templates.setSelectedIndex(0)
  templates.setFont(new Font("Arial", Font.BOLD, 18))
  templates.setCellRenderer(new TemplateCellRenderer)

  add(templates, "grow, push")

}
