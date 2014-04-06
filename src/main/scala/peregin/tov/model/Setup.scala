package peregin.tov.model

import org.json4s.jackson.Serialization.{read, write}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization


object Setup {

  implicit val formats = Serialization.formats(NoTypeHints)
  def load(json: String): Setup = read[Setup](json)
}

case class Setup(videoPath: Option[String],
            telemetryPath: Option[String]) {

  implicit val formats = Serialization.formats(NoTypeHints)
  def save: String = write(this)
}
