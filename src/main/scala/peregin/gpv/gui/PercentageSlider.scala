package peregin.gpv.gui

import java.awt.event.{MouseAdapter, MouseEvent}

import javax.swing.JSlider
import peregin.gpv.util.Logging

import scala.swing.event.ValueChanged
import scala.swing.{Component, Orientable, Publisher}

/**
 * Stores a percentage value with 2 decimal places.
 * - the current value is stored in a double between [0.00 - 100.00]
 * - won't trigger an update when the current value is set programmatically, so it's easy to track the progress of
 * a video stream for example...
 *
 * @author levi@peregin.com
 * @see javax.swing.JSlider
 */
class PercentageSlider extends Component with Orientable.Wrapper with Publisher with Logging {

  override lazy val peer: JSlider = new JSlider(0, 10000, 0) with SuperMixin

  import javax.swing.UIDefaults
  import javax.swing.UIManager
  import javax.swing.plaf.basic.BasicSliderUI

  val defaults: UIDefaults = UIManager.getLookAndFeelDefaults
  val sliderClass = defaults.getUIClass("SliderUI")
  val methodXValForPos = classOf[BasicSliderUI].getDeclaredMethod("valueForXPosition", classOf[Int])
  methodXValForPos.setAccessible(true)
  val methodIsDragging = classOf[BasicSliderUI].getDeclaredMethod("isDragging")
  methodIsDragging.setAccessible(true)

  @volatile private var sliderChangeFromApi = true

  peer.setPaintTrack(true)
  peer.setPaintTicks(true)
  peer.setMajorTickSpacing(1000)
  peer.setMinorTickSpacing(100)
  peer.setSnapToTicks(false)

  peer.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      val dragging = methodIsDragging.invoke(peer.getUI).asInstanceOf[Boolean]
      if (!dragging) {
        val xSlider = methodXValForPos.invoke(peer.getUI, new Integer(e.getX)).asInstanceOf[Integer]
        val xSlideTo = xSlider.toDouble / 100
        //debug(s"EVENT-M: sliding to $xSlideTo % , when dragging=$dragging, on event $e")
        percentage = xSlideTo // set the slider value
      }
    }
  })

  peer.addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e: javax.swing.event.ChangeEvent) {
      if (!peer.getValueIsAdjusting && !sliderChangeFromApi) {
        //debug(s"EVENT-C: value changed on event $e")
        publish(new ValueChanged(PercentageSlider.this))
      }
    }
  })

  def percentage: Double = peer.getValue.toDouble / 100
  def percentage_=(v: Double) {
    sliderChangeFromApi = true
    peer.setValue((v * 100).toInt)
    sliderChangeFromApi = false
  }
}
