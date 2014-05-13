package peregin.gpv.util

import org.joda.time.Duration
import org.joda.time.format.PeriodFormatterBuilder


object DurationPrinter {

  lazy val formatter = new PeriodFormatterBuilder()
    .appendDays()
    .appendSuffix("d")
    .appendHours()
    .appendSuffix("h")
    .appendMinutes()
    .appendSuffix("m")
    .appendSeconds()
    .appendSuffix("s")
    .toFormatter

  def print(elapsedInMillis: Option[Long]): String = print(elapsedInMillis.getOrElse(0L))

  def print(elapsedInMillis: Long) = formatter.print(new Duration(elapsedInMillis).toPeriod)
}
