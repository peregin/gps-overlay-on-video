package peregin.gpv.util

import com.fasterxml.jackson.core.JsonParser.Feature
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.json4s.jackson.Json4sScalaModule
import org.json4s.{DefaultFormats, jackson}

import scala.annotation.nowarn


object JsonConverter {

  @nowarn
  private val mapper: ObjectMapper = new ObjectMapper()
    .registerModule(new DefaultScalaModule())
    .registerModule(new Json4sScalaModule())
    .registerModule(new JavaTimeModule())
    .configure(SerializationFeature.INDENT_OUTPUT, true)
    .configure(Feature.ALLOW_TRAILING_COMMA, true)

  implicit val formats: DefaultFormats.type = DefaultFormats //+ FieldSerializer[Setup](ignore("telemetry"))

  def generate[T <: AnyRef](obj: T): String = {
    //val ast = jackson.Json(formats, mapper = mapper)
    //ast.writePretty(obj)
    mapper.writeValueAsString(obj)
  }

  def parse[T](json: String)(implicit mf: Manifest[T]): T = {
    //val ast = jackson.Json(formats, mapper = mapper).parse(json)
    //ast.extract(formats, mf)
    mapper.readValue(json, mf.runtimeClass).asInstanceOf[T]
  }
}
