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

  // this slider is driven purely by the mouse (see the listeners below, which derive the percentage
  // from the pointer's x position). It must not take keyboard focus: JSlider's own default arrow-key
  // bindings would otherwise intercept the arrow keys (e.g. stealing them from the global video-seek
  // shortcuts) and, because the change listener below recomputes the percentage from the *last mouse*
  // position rather than the slider's new value, a key-driven change silently snaps back to wherever
  // the pointer last was instead of moving at all.
  peer.setFocusable(false)

  def orientation: Orientation.Value = Orientation(peer.getOrientation)
  def orientation_=(o: Orientation.Value): Unit = peer.setOrientation(o.id)

  import javax.swing.plaf.basic.BasicSliderUI

  private val methodXValForPos = classOf[BasicSliderUI].getDeclaredMethod("valueForXPosition", classOf[Int])
  methodXValForPos.setAccessible(true)
  private val methodIsDragging = classOf[BasicSliderUI].getDeclaredMethod("isDragging")
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
        lastEventX = e.getX
        val xSlideTo = calculatePercentage(lastEventX)
        percentage = xSlideTo // set the slider value
      }
    }
  })

  peer.addChangeListener((_: javax.swing.event.ChangeEvent) => {
    if (!peer.getValueIsAdjusting && !sliderChangeFromApi) {
      // report the slider's own current value rather than recomputing it from the last recorded mouse
      // x position: lastEventX is only ever updated by real mouse presses/drags, so any change event
      // that fires for another reason (a stray/racing programmatic update slipping past the
      // sliderChangeFromApi guard, for example) would otherwise be reported using a stale, unrelated
      // position instead of harmlessly reflecting whatever the slider's value already is
      publish(new SliderChanged(PercentageSlider.this, percentage))
    }
  })

  def percentage: Double = peer.getValue.toDouble / 100
  def percentage_=(v: Double): Unit = {
    sliderChangeFromApi = true
    peer.setValue((v * 100).toInt)
    sliderChangeFromApi = false
  }

  private def calculatePercentage(x: Int): Double = {
    val xSlider = methodXValForPos.invoke(peer.getUI, Integer.valueOf(x)).asInstanceOf[Integer]
    xSlider.toDouble / 100
  }
}
