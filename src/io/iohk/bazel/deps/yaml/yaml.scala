package io.iohk.bazel.deps

import org.json4s.JValue
import org.json4s.jackson.Json4sScalaModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.databind.ObjectMapper

package object yaml {

  def parseYaml(yaml: String): JValue = {
    val yamlMapper = new ObjectMapper(new YAMLFactory())
    yamlMapper.registerModule(new Json4sScalaModule)
    val yamlReader = yamlMapper.readerFor(classOf[JValue])
    yamlReader.readValue(yaml)
  }

  def read[T: Read]: Read[T] = implicitly[Read[T]]

  implicit class StringOps(val str: String) extends AnyVal {
    def parsedYaml: JValue = yaml.parseYaml(str)
  }

  implicit class JVAlueOps(val j: JValue) extends AnyVal {
    def as[T: Read]: Either[String, T] = implicitly[Read[T]].read(j)
  }
}
