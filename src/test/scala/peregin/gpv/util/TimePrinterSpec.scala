package peregin.gpv.util

import org.specs2.mutable.Specification
import TimePrinter._

/**
 * Created by peregin on 04/11/14.
 */
class TimePrinterSpec extends Specification {

  addArguments(stopOnFail)

  "duration printer" should {
    "show elapsed time formatted as hh:mm:dd.sss" in {
      printDuration(62001) == "00:01:02.001"
    }

    "parse text to elapsed duration" in {
      text2Duration("00:01:02.001") == 62001
    }
  }
}
