package peregin.gpv.util

import java.io.Closeable


object Io {

  def withCloseable[R](c: Closeable)(body: Closeable => R): R = try {
    body(c)
  } finally {
    c.close()
  }
}
