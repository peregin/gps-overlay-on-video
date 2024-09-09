package peregin.gpv.model

import org.specs2.mutable.Specification
import peregin.gpv.util.Logging
import peregin.gpv.Setup


class SetupSpec extends Specification with Logging {

  stopOnFail

  "an instance of the setup" should {
    "be serialized and deserialized" in {
      val s1 = Setup(Some("video/path"), Some("telemetry/path"), None, Some(10L), Some(60d), Some("Metric"), None, Some(10000), Seq())
      val json = s1.save
      log.debug(s"json:\n$json\n")
      val s2 = Setup.load(json)
      s1 === s2
    }
  }
}
