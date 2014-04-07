package peregin.tov.model

import org.json4s.jackson.Serialization.{read, write}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import scala.io.Source
import java.io.FileWriter
import java.nio.file.{Paths, Files}


object Setup {

  implicit val formats = Serialization.formats(NoTypeHints)

  def save(setup: Setup): String = write(setup)
  def saveFile(file: String, setup: Setup) {
    val fw = new FileWriter(file)
    fw.write(save(setup))
    fw.close()
  }

  def load(json: String): Setup = read[Setup](json)
  def loadFile(file: String): Setup = read(new String(Files.readAllBytes(Paths.get(file))))

  def empty = new Setup(None, None)
}

case class Setup(var videoPath: Option[String],
                 var telemetryPath: Option[String]) {

  def save = Setup.save(this)
  def saveFile(path: String) = Setup.saveFile(path, this)

  def reset() {
    videoPath = None
    telemetryPath = None
  }

  def copyAs(that: Setup) {
    this.videoPath = that.videoPath
    this.telemetryPath = that.videoPath
  }
}
