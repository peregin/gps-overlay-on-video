package peregin.gpv.util

trait Timed {

  def timed[T](s: => String)(body: => T): T = {
    val mark = System.currentTimeMillis()
    val ret = body
    val elapsed = System.currentTimeMillis() - mark
    println(s"Executed $s in $elapsed millis")
    ret
  }
}
