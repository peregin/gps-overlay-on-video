package peregin.gpv.gui

import java.awt.event.{ActionEvent, ActionListener}

import org.jdesktop.swingx.JXButton
import peregin.gpv.util.Io


class StartStopButton[T](playImage: String, playTooltip: String, stopImage: String, stopTooltip: String, action: => T) extends JXButton {

  private val playIcon = Io.loadIcon(playImage)
  private val stopIcon = Io.loadIcon(stopImage)
  private var playing = false

  stop()

  addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent) = action
  })

  def isPlaying = playing

  def play(): Unit = {
    playing = true
    setToolTipText(stopTooltip)
    setIcon(stopIcon)
  }

  def stop(): Unit = {
    playing = false
    setToolTipText(playTooltip)
    setIcon(playIcon)
  }
}
