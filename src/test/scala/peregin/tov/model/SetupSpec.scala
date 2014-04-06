package peregin.tov.model

import org.specs2.mutable.Specification


class SetupSpec extends Specification {

  addArguments(stopOnFail)

  "an instance of the setup" should {
    "be serialized and deserialized" in {
      val s1 = Setup(Some("video/path"), Some("telemetry/path"))
      val json = s1.save
      val s2 = Setup.load(json)
      s1 === s2
    }
  }
}
