package peregin.gpv.model

import org.specs2.mutable.Specification

class InputValueSpec extends Specification {

  "input" should {
    "be in top %" in {
      InputValue(Some(95), MinMax(0, 100)).isInTop(10) must beTrue
      InputValue(Some(91), MinMax(0, 100)).isInTop(10) must beTrue
      InputValue(Some(89), MinMax(0, 100)).isInTop(10) must beFalse
      InputValue(Some(90), MinMax(-100, 100)).isInTop(25) must beTrue
      InputValue(Some(40), MinMax(-100, 100)).isInTop(25) must beFalse
    }
  }
}
