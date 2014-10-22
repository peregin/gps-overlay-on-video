package peregin.gpv.util


import org.joda.time.Duration
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, PeriodFormatterBuilder}


object TimePrinter {

  lazy val durationFormatter = new PeriodFormatterBuilder()
    .appendDays()
    .appendSuffix("d")
    .appendHours()
    .appendSuffix("h")
    .appendMinutes()
    .appendSuffix("m")
    .appendSeconds()
    .appendSuffix("s")
    .appendMillis3Digit()
    .appendSuffix("millis")
    .toFormatter

  lazy val timeFormatter = DateTimeFormat.forPattern("hh:mm:ss.SSS")

  def printDuration(elapsedInMillisOption: Option[Long]): String = elapsedInMillisOption.map(printDuration).getOrElse("")
  def printDuration(elapsedInMillis: Long) = durationFormatter.print(new Duration(elapsedInMillis).toPeriod)

  def printTime(timeInMillisOption: Option[Long]): String = timeInMillisOption.map(printTime).getOrElse("")
  def printTime(timeInMillis: Long) = timeFormatter.print(timeInMillis)
}
