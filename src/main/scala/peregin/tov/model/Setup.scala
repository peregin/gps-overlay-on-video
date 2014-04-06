package peregin.tov.model

import org.json4s.jackson.Serialization.{read, write}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import scala.io.Source
import java.io.FileWriter


object Setup {

  implicit val formats = Serialization.formats(NoTypeHints)
  def load(json: String): Setup = read[Setup](json)
  def loadFile(file: String): Setup = read(Source.fromFile(file).getLines().mkString("\n"))
}

case class Setup(videoPath: Option[String],
            telemetryPath: Option[String]) {

  implicit val formats = Serialization.formats(NoTypeHints)
  def save: String = write(this)
  def saveFile(file: String) {
    val fw = new FileWriter(file)
    fw.write(save)
    fw.close()
  }
}
