package peregin.gpv.gui

import org.jdesktop.swingx.JXButton
import java.awt.event.{ActionEvent, ActionListener}
import peregin.gpv.util.Io


class ImageButton[T](image: String, tooltip: String, action: => T) extends JXButton(Io.loadIcon(image)) {
  setToolTipText(tooltip)
  addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) = action
  })
}
