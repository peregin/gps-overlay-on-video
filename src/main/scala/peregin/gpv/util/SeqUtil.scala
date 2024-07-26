package peregin.gpv.util

import java.util.function.{Predicate}


object SeqUtil {

  /**
   * Finds the latest value in the reversed sequence, i.e. the earliest value in original sequence.
   *
   * @param seq
   *    sequence to search
   * @param matcher
   *    predicate to test values
   * @tparam T
   *    type of element
   *
   * @return
   *    the leftmost value where this and following satisfy condition, or empty if no found.
   */
  def findReverseTill[T](seq: Seq[T], matcher: Predicate[T]): Option[T] = {
    var found: Option[T] = None
    val it = seq.reverseIterator
    while (it.hasNext) {
      val el: T = it.next()
      if (matcher.test(el)) {
        found = Some(el)
      }
      else {
        return found
      }
    }
    return found
  }

  /**
   * Returns the index with greatest value less than or equal to the given value, or -1 if there is no such key.
   */
  def floorIndex[K, T](seq: Seq[T], value: K, extractFunc: T => K, comparator: (K, K) => Int): Int = {
    var ( b, e)  = ( 0, seq.size )
    var found = -1
    while (b < e) {
      val m = b + (e - b) / 2
      val c = extractFunc(seq(m))
      val r = comparator(value, c)
      if (r < 0) {
        e = m
      }
      else {
        found = m
        b = m
        if (b >= e - 1) {
          return found
        }
      }
    }
    found
  }

}
