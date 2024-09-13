package peregin.gpv.gui

import java.awt.{Color, Font}
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{JSlider, JSpinner}

import peregin.gpv.gui.gauge.{GaugePainter, GaugeComponent}
import peregin.gpv.model.{InputValue, MinMax}

import scala.swing.Label

/**
 * Created by levi on 26/12/14.
 */
class GaugeTestPanel[T <: GaugeComponent](factory: => T) extends MigPanel("ins 5, fill", "[fill]", "[fill]") {

  val gauges: List[GaugeComponent] = List.fill(5)(factory)
  gauges.foreach(_.gaugePainter.debug = true)

  val minSpinner = new JSpinner
  minSpinner.setValue(gauges(0).gaugePainter.defaultInput.boundary.min.toInt)
  minSpinner.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = minValueAdjusted()
  })

  val maxSpinner = new JSpinner
  maxSpinner.setValue(gauges(0).gaugePainter.defaultInput.boundary.max.toInt)
  maxSpinner.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = maxValueAdjusted()
  })

  val slider = new JSlider(gauges(0).gaugePainter.defaultInput.boundary.min.toInt, gauges(0).gaugePainter.defaultInput.boundary.max.toInt, gauges(0).gaugePainter.defaultInput.current.getOrElse(0d).toInt)
  slider.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = curValueAdjusted()
  })

  val status = new Label(s"Current Value ${slider.getValue}")
  status.font = new Font("Verdana", Font.BOLD, 16)

  background = Color.lightGray

  add(gauges(0), "w 32px, h 32px, wrap")
  add(gauges(1), "w 50px, h 50px")
  add(status, "wrap")
  add(gauges(2), "w 100px, h 100px, wrap")
  add(gauges(3), "w 200px, h 200px")
  add(gauges(4), "w 300px, h 300px, wrap")
  val controls = new MigPanel("", "[fill]", "[fill]") {
    add(new Label("Min"), "alignx left")
    add(minSpinner, "alignx left, wmin 50")
    add(slider, "pushx, growx")
    add(maxSpinner, "alignx left, wmin 50")
    add(new Label("Max"), "alignx right")
  }
  add(controls, "span 2, wrap")


  curValueAdjusted()


  def minValueAdjusted(): Unit = {
    val min = minSpinner.getValue.asInstanceOf[Int]
    slider.setMinimum(min)
    updateGui(min, maxSpinner.getValue.asInstanceOf[Int], slider.getValue)
  }
  def maxValueAdjusted(): Unit = {
    val max = maxSpinner.getValue.asInstanceOf[Int]
    slider.setMaximum(max)
    updateGui(minSpinner.getValue.asInstanceOf[Int], max, slider.getValue)
  }
  def curValueAdjusted(): Unit = {
    updateGui(minSpinner.getValue.asInstanceOf[Int], maxSpinner.getValue.asInstanceOf[Int], slider.getValue)
  }

  def updateGui(min: Int, max: Int, cur: Int): Unit = {
    status.text = s"Current Value $cur"
    gauges.foreach{g =>
      g.gaugePainter.input = InputValue(Some(cur), MinMax(min, max))
      g.repaint()
    }
  }
}