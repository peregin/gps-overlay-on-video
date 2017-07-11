package peregin.gpv.model

import org.specs2.mutable.Specification
import MinMax._


class MinMaxSpec extends Specification {

  addArguments(stopOnFail)

  "an instance" should {
    val mm = MinMax(0, 3)
    "match if value is inside the range" in {
       mm.includes(-0.1) must beFalse
       mm.includes(0) must beTrue
       mm.includes(1.2) must beTrue
       mm.includes(2.99) must beTrue
       mm.includes(3) must beFalse
       mm.includes(32) must beFalse
    }
  }

  "rounder" should {

    "round up to nearest tenth" in {
      27.2.roundUpToTenth === 30
      21.5.roundUpToTenth === 30
      30.roundUpToTenth === 30
      0.roundUpToTenth === 0
      -11.roundUpToTenth === -10
    }

    "round down to nearest tenth" in {
      27.2.roundDownToTenth === 20
      21.5.roundDownToTenth === 20
      30.roundDownToTenth === 30
      0.roundDownToTenth === 0
      -11.roundDownToTenth === -20
    }

    "round up to nearest hundredth" in {
      27.2.roundUpToHundredth === 100
      210.5.roundUpToHundredth === 300
      301.roundUpToHundredth === 400
      0.roundUpToHundredth === 0
      -11.roundUpToHundredth === 0
    }

    "round down to nearest hundredth" in {
      210.5.roundDownToHundredth === 200
      301.roundDownToHundredth === 300
      0.roundDownToHundredth === 0
      -11.roundDownToHundredth === -100
    }
  }
}
