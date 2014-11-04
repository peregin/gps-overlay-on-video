package peregin.gpv.gui

import javax.swing.JSlider

import peregin.gpv.util.Logging

import scala.swing.event.ValueChanged
import scala.swing.{Publisher, Orientable, Component}

/**
 * Stores a percentage value with 2 decimal places.
 * - the current value is stored in a double between [0.00 - 100.00]
 * - won't trigger an update when the current value is set programmatically, so it's easy to track the progress of
 * a video stream for example...
 *
 * @author levi@peregin.com
 * @see javax.swing.JSlider
 */
class PercentageSlider extends Component with Orientable.Wrapper with Publisher {

  override lazy val peer: JSlider = new JSlider(0, 10000, 0) with SuperMixin

  @volatile private var sliderChangeFromApi = true

  peer.setPaintTrack(true)
  peer.setPaintTicks(true)
  peer.setMajorTickSpacing(1000)
  peer.setMinorTickSpacing(100)

  peer.addChangeListener(new javax.swing.event.ChangeListener {
    def stateChanged(e: javax.swing.event.ChangeEvent) {
      if (!peer.getValueIsAdjusting && !sliderChangeFromApi) {
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
