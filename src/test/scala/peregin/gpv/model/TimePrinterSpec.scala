package peregin.gpv.model

import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mutable.Specification
import peregin.gpv.util.TimePrinter._


class TimePrinterSpec extends Specification {

  "duration printer" should {
    "convert elapsed time" in {
      printDuration(62001) === "00:01:02.001"
    }
  }

  "time printer" should {
    "convert an arbitrary time" in {
      val time = new DateTime(2014, 11, 20, 16, 1, 23, 100, DateTimeZone.forID("Europe/Zurich"))
      val text = printTime(time.getMillis)
      text.endsWith("01:23.100") must beTrue
      text.length === "04:01:23.100".length
    }
  }
}