package peregin.gpv.gui

import scala.swing.{Component, Panel}
import net.miginfocom.swing.MigLayout
import javax.swing.JComponent


class MigPanel(layoutConstraints: String, colConstraints: String, rowConstraints: String) extends Panel {
  peer.setLayout(new MigLayout(layoutConstraints, colConstraints, rowConstraints))

  def add(comp: Component, constraint: String): Unit = {
    peer.add(comp.peer, constraint)
  }

  implicit def swing2scala(c: JComponent): Component = Component.wrap(c)
}
