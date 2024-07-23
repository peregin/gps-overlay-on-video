package peregin.gpv.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.yaml.snakeyaml.Yaml

import java.io.InputStream
import scala.reflect.ClassTag


object YamlConverter {
  private val snakeYaml: Yaml = new Yaml()
  private val objectMapper: ObjectMapper = new ObjectMapper(new YAMLFactory());

  objectMapper.registerModule(DefaultScalaModule)

  def read[T](input: InputStream)(implicit ct: ClassTag[T]): T = {
    val map: java.util.Map[String, Any]  = snakeYaml.load(input)
    return objectMapper.convertValue(map, ct.runtimeClass).asInstanceOf[T]
  }
}
