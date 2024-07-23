package peregin.gpv.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.InputStream
import scala.reflect.ClassTag


object YamlConverter {

  private val objectMapper: ObjectMapper = new ObjectMapper(new YAMLFactory());

  objectMapper.registerModule(DefaultScalaModule)

  def read[T](input: InputStream)(implicit ct: ClassTag[T]): T = {
    return objectMapper.readValue(input, ct.runtimeClass).asInstanceOf[T]
  }
}
