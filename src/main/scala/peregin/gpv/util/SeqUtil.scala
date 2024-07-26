package peregin.gpv.util

import java.util.function.Predicate


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
}
