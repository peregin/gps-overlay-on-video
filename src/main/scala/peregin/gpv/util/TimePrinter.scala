package peregin.gpv.util


import org.joda.time.Duration
import org.joda.time.format.{DateTimeFormat, PeriodFormatterBuilder}


object TimePrinter {

  lazy val durationFormatter = new PeriodFormatterBuilder()
    .printZeroAlways().minimumPrintedDigits(2)
    .appendHours()
    .appendSuffix(":")
    .appendMinutes()
    .appendSuffix(":")
    .appendSeconds()
    .appendSuffix(".")
    .appendMillis3Digit()
    .toFormatter

  lazy val timeFormatter = DateTimeFormat.forPattern("hh:mm:ss.SSS")

  def printDuration(elapsedInMillisOption: Option[Long]): String = elapsedInMillisOption.map(printDuration).getOrElse("")
  def printDuration(elapsedInMillis: Long) = durationFormatter.print(new Duration(elapsedInMillis).toPeriod)
  def text2Duration(text: String): Long = {
    val parsePeriod = durationFormatter.parsePeriod(text)
    parsePeriod.toStandardDuration.getMillis
  }

  def printTime(timeInMillisOption: Option[Long]): String = timeInMillisOption.map(printTime).getOrElse("")
  def printTime(timeInMillis: Long) = timeFormatter.print(timeInMillis)
}
