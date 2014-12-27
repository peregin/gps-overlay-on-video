package peregin.gpv.gui

import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.{SpinnerDateModel, JSpinner}
import java.util.Calendar
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import peregin.gpv.util.Logging

import scala.swing.{Component, Publisher}
import scala.swing.event.ValueChanged


class DurationSpinner extends JSpinner(new SpinnerDateModel()) with Publisher with Logging {

  val timeEditor = new JSpinner.DateEditor(this, "HH:mm:ss.SSS")
  setEditor(timeEditor)
  val cal = Calendar.getInstance()

  addChangeListener(new ChangeListener {
    override def stateChanged(e: ChangeEvent) {
      publish(new ValueChanged(Component.wrap(DurationSpinner.this)))
    }
  })

  duration = 0L

  def duration_= (millis: Long) {
    val d = Duration(millis, TimeUnit.MILLISECONDS)
    var t = millis
    val hours = d.toHours.toInt
    t -= TimeUnit.HOURS.toMillis(hours)
    cal.set(Calendar.HOUR_OF_DAY, hours)
    val minutes = d.toMinutes.toInt
    t -= TimeUnit.MINUTES.toMillis(minutes)
    cal.set(Calendar.MINUTE, minutes)
    val seconds = d.toSeconds.toInt
    t -= TimeUnit.SECONDS.toMillis(seconds)
    val ms = t
    cal.set(Calendar.SECOND, seconds)
    cal.set(Calendar.MILLISECOND, ms.toInt)
    debug(s"hour=$hours min=$minutes sec=$seconds mill=$ms")
    setValue(cal.getTime)
  }

  def duration: Long = {
    cal.setTime(getValue.asInstanceOf[java.util.Date])
    val hours = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)
    val millis = cal.get(Calendar.MILLISECOND)
    import TimeUnit._
    HOURS.toMillis(hours) + MINUTES.toMillis(minutes) + SECONDS.toMillis(seconds) + millis
  }
}
