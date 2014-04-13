package peregin.gpv.util

import org.json4s.{jackson, DefaultFormats}


object JsonConverter {

  implicit val formats = DefaultFormats //+ FieldSerializer[Setup](ignore("telemetry"))

  def generate[T <: AnyRef](obj: T): String = {
    val ast = jackson.Json(formats)
    ast.writePretty(obj)
  }

  def parse[T](json: String)(implicit mf: Manifest[T]): T = {
    val ast = jackson.Json(formats).parse(json)
    ast.extract(formats, mf)
  }
}
