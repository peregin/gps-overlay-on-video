package peregin.gpv.gui

import javax.swing.{SpinnerDateModel, JSpinner}
import java.util.Calendar
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


class DurationSpinner extends JSpinner(new SpinnerDateModel()) {

  val timeEditor = new JSpinner.DateEditor(this, "HH:mm:ss.SSS")
  setEditor(timeEditor)
  val cal = Calendar.getInstance()

  duration = 0L

  def duration_= (millis: Long) {
    val d = Duration(millis, TimeUnit.MILLISECONDS)
    cal.set(Calendar.HOUR_OF_DAY, d.toHours.toInt)
    cal.set(Calendar.MINUTE, d.toMinutes.toInt)
    cal.set(Calendar.SECOND, d.toSeconds.toInt)
    cal.set(Calendar.MILLISECOND, d.toMillis.toInt)
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
