package peregin.tov.util

import java.io.Closeable


object Io {

  def withCloseable[T](c: Closeable)(body: => T): T = {
    try {
      body
    } finally {
      c.close()
    }
  }
}
