package peregin.gpv.util

import org.joda.time.DateTime
import org.scalacheck.Prop
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import peregin.gpv.util.TimePrinter._

/**
 * Created by peregin on 04/11/14.
 */
class TimePrinterSpec extends Specification with ScalaCheck {

  stopOnFail

  "duration printer" should {
    "show elapsed time formatted as hh:mm:dd.sss" in {
      printDuration(62001) === "00:01:02.001"
    }

    "parse text to elapsed duration" in {
      text2Duration("00:01:02.001") === 62001
    }
  }

  "time printer" should {
    "convert an arbitrary time" in {
      val time = new DateTime(2014, 11, 20, 16, 1, 23, 100)
      printTime(time.getMillis) === "04:01:23.100"
    }
  }

  "converter" in {
    "generate and parse the same timestamp" ! Prop.forAll { (ts: Int) =>
      val text = printDuration(ts)
      val reverseTs = text2Duration(text)
      ts === reverseTs
    }.set(minTestsOk = 100)
  }
}
