package peregin.gpv.model

import org.specs2.mutable.Specification


class SlopeSegmentSpec extends Specification {

  "an instance" should {
    "have prooper toString" in {
      SlopeSegment.Easy.toString === "Easy (0 - 4)%"
    }
  }
}
