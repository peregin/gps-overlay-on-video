package peregin.tov.util

import org.json4s.{jackson, DefaultFormats}


object JsonConverter {

  // enrich with custom formatters or FieldSerializer to ignore specific fields
  implicit val formats = DefaultFormats

  def generate[T <: AnyRef](obj: T): String = {
    val ast = jackson.Json(formats)
    ast.writePretty(obj)
  }

  def parse[T](json: String)(implicit mf: Manifest[T]): T = {
    val ast = jackson.Json(formats).parse(json)
    ast.extract(formats, mf)
  }
}
