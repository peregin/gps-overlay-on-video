package peregin.gpv.util

import org.specs2.mutable.Specification


/**
 * Created by Zbynek Vyskovsky on 2024-07-26.
 */
class SeqUtilSpec extends Specification {

  "floorIndex" should {
    "first in even" in {
      val r = SeqUtil.floorIndex(Seq(1, 2, 3, 4), 1, (el: Int) => el, (a: Int, b: Int) => a - b)
      r mustEqual(0)
    }

    "first in odd" in {
      val r = SeqUtil.floorIndex(Seq(1, 2, 3, 4, 5), 1, (el: Int) => el, (a: Int, b: Int) => a - b)
      r mustEqual(0)
    }

    "last in even" in {
      val r = SeqUtil.floorIndex(Seq(1, 2, 3, 4), 4, (el: Int) => el, (a: Int, b: Int) => a - b)
      r mustEqual(3)
    }

    "last in odd" in {
      val r = SeqUtil.floorIndex(Seq(1, 2, 3, 4, 5), 5, (el: Int) => el, (a: Int, b: Int) => a - b)
      r mustEqual(4)
    }

    "lower only" in {
      val r = SeqUtil.floorIndex(Seq(0, 1, 3, 4), 2, (el: Int) => el, (a: Int, b: Int) => a - b)
      r mustEqual(1)
    }

    "repeated should return last" in {
      val r = SeqUtil.floorIndex(Seq(0, 1, 1, 1, 3, 4), 2, (el: Int) => el, (a: Int, b: Int) => a - b)
      r mustEqual(3)
    }
  }

}
