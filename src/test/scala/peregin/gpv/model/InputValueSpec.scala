package peregin.gpv.model

import org.specs2.mutable.Specification

class InputValueSpec extends Specification {

  "input" should {
    "be in top %" in {
      InputValue(95, MinMax(0, 100)).isInTop(10) must beTrue
      InputValue(91, MinMax(0, 100)).isInTop(10) must beTrue
      InputValue(89, MinMax(0, 100)).isInTop(10) must beFalse
      InputValue(90, MinMax(-100, 100)).isInTop(25) must beTrue
      InputValue(40, MinMax(-100, 100)).isInTop(25) must beFalse
    }
  }
}
