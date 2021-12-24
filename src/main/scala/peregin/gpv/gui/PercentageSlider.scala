package peregin.gpv.gui

import java.awt.event.{MouseAdapter, MouseEvent, MouseMotionAdapter}
import javax.swing.JSlider
import peregin.gpv.util.Logging

import scala.swing.event.ComponentEvent
import scala.swing.{Component, Orientable, Orientation, Publisher}

object SliderChanged {
  def unapply(a: SliderChanged): Option[(Component, Double)] = Some((a.source, a.percentage))
}

case class SliderChanged(override val source: Component, percentage: Double) extends ComponentEvent

/**
 * Stores a percentage value with 2 decimal places.
 * - the current value is stored in a double between [0.00 - 100.00]
 * - won't trigger an update when the current value is set programmatically, so it's easy to track the progress of
 * a video stream for example...
 *
 * @author levi@peregin.com
 * @see javax.swing.JSlider
 */
class PercentageSlider extends Component with Orientable with Publisher with Logging {

  override lazy val peer: JSlider = new JSlider(0, 10000, 0) with SuperMixin

  def orientation: Orientation.Value = Orientation(peer.getOrientation)
  def orientation_=(o: Orientation.Value): Unit = peer.setOrientation(o.id)

  import javax.swing.plaf.basic.BasicSliderUI

  val methodXValForPos = classOf[BasicSliderUI].getDeclaredMethod("valueForXPosition", classOf[Int])
  methodXValForPos.setAccessible(true)
  val methodIsDragging = classOf[BasicSliderUI].getDeclaredMethod("isDragging")
  methodIsDragging.setAccessible(true)

  @volatile private var sliderChangeFromApi = true
  @volatile private var lastEventX = 0

  peer.setPaintTrack(true)
  peer.setPaintTicks(true)
  peer.setMajorTickSpacing(1000)
  peer.setMinorTickSpacing(100)
  peer.setSnapToTicks(false)

  peer.addMouseMotionListener(new MouseMotionAdapter {

    override def mouseDragged(e: MouseEvent): Unit = {
      val dragging = methodIsDragging.invoke(peer.getUI).asInstanceOf[Boolean]
      if (dragging) {
        lastEventX = e.getX
      }
    }
  })

  peer.addMouseListener(new MouseAdapter {

    override def mousePressed(e: MouseEvent): Unit = {
      val dragging = methodIsDragging.invoke(peer.getUI).asInstanceOf[Boolean]
      if (!dragging) {
        lastEventX = e.getX()
        val xSlideTo = calculatePercentage(lastEventX)
        percentage = xSlideTo // set the slider value
      }
    }
  })

  peer.addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e: javax.swing.event.ChangeEvent): Unit = {
      if (!peer.getValueIsAdjusting && !sliderChangeFromApi) {
        val xSlideTo = calculatePercentage(lastEventX)
        publish(new SliderChanged(PercentageSlider.this, xSlideTo))
      }
    }
  })

  def percentage: Double = peer.getValue.toDouble / 100
  def percentage_=(v: Double): Unit = {
    sliderChangeFromApi = true
    peer.setValue((v * 100).toInt)
    sliderChangeFromApi = false
  }

  private def calculatePercentage(x: Int): Double = {
    val xSlider = methodXValForPos.invoke(peer.getUI, new Integer(x)).asInstanceOf[Integer]
    xSlider.toDouble / 100
  }
}
