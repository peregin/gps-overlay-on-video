package peregin.gpv.manual

import java.awt.{Color, Font}
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{JSlider, JSpinner}

import info.BuildInfo
import peregin.gpv.gui.gauge._
import peregin.gpv.gui.{Goodies, MigPanel}
import peregin.gpv.model.{InputValue, MinMax}
import peregin.gpv.util.Logging

import scala.swing._


object GaugeManualTest extends SimpleSwingApplication with Logging {

  Goodies.initLookAndFeel()

  val gauges = List.fill(4)(new GaugeComponent with RadialSpeedGauge)
  gauges.foreach(_.debug = true)

  val minSpinner = new JSpinner
  minSpinner.setValue(gauges(0).defaultInput.boundary.min.toInt)
  minSpinner.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = minValueAdjusted()
  })

  val maxSpinner = new JSpinner
  maxSpinner.setValue(gauges(0).defaultInput.boundary.max.toInt)
  maxSpinner.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = maxValueAdjusted()
  })

  val slider = new JSlider(gauges(0).defaultInput.boundary.min.toInt, gauges(0).defaultInput.boundary.max.toInt, gauges(0).defaultInput.current.toInt)
  slider.addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) = curValueAdjusted()
  })

  val status = new Label(s"Current Value ${slider.getValue}")
  status.font = new Font("Verdana", Font.BOLD, 16)

  val frame = new MainFrame {
    title = s"Gauge Test Container - built ${BuildInfo.buildTime}"
    contents = new MigPanel("ins 5, fill", "[fill]", "[fill]") {
      background = Color.lightGray

      add(gauges(0), "w 50px, h 50px")
      add(status, "wrap")
      add(gauges(1), "w 100px, h 100px, wrap")
      add(gauges(2), "w 200px, h 200px")
      add(gauges(3), "w 300px, h 300px, wrap")
      val controls = new MigPanel("", "[fill]", "[fill]") {
        add(new Label("Min"), "alignx left")
        add(minSpinner, "alignx left, wmin 50")
        add(slider, "pushx, growx")
        add(maxSpinner, "alignx left, wmin 50")
        add(new Label("Max"), "alignx right")
      }
      add(controls, "span 2, wrap")
    }
  }
  override def top = frame

  Goodies.center(frame)
  curValueAdjusted()

  def minValueAdjusted() {
    val min = minSpinner.getValue.asInstanceOf[Int]
    slider.setMinimum(min)
    updateGui(min, maxSpinner.getValue.asInstanceOf[Int], slider.getValue)
  }
  def maxValueAdjusted() {
    val max = maxSpinner.getValue.asInstanceOf[Int]
    slider.setMaximum(max)
    updateGui(minSpinner.getValue.asInstanceOf[Int], max, slider.getValue)
  }
  def curValueAdjusted() {
    updateGui(minSpinner.getValue.asInstanceOf[Int], maxSpinner.getValue.asInstanceOf[Int], slider.getValue)
  }

  def updateGui(min: Int, max: Int, cur: Int) {
    status.text = s"Current Value $cur"
    gauges.foreach{g =>
      g.input = InputValue(cur, MinMax(min, max))
      g.repaint()
    }
  }
}
