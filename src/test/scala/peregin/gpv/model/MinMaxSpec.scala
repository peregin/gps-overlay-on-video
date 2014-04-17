package peregin.gpv.model

import org.specs2.mutable.Specification


class MinMaxSpec extends Specification {

  addArguments(stopOnFail)

  "rounder" should {
    import MinMax._
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
  }
}
