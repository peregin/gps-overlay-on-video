package peregin.gpv.gui

import scala.swing.{Component, Panel}
import net.miginfocom.swing.MigLayout


class MigPanel(layoutConstraints: String, colConstraints: String, rowConstraints: String) extends Panel {
  peer.setLayout(new MigLayout(layoutConstraints, colConstraints, rowConstraints))

  def add(comp: Component, constraint: String) {
    peer.add(comp.peer, constraint)
  }
}
