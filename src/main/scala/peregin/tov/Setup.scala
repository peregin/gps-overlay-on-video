package peregin.tov

import java.io.FileWriter
import peregin.tov.util.JsonConverter
import scala.io.Source


object Setup {

  def save(setup: Setup): String = JsonConverter.generate(setup)
  def saveFile(file: String, setup: Setup) {
    val fw = new FileWriter(file)
    fw.write(save(setup))
    fw.close()
  }

  def load(json: String): Setup = JsonConverter.parse[Setup](json)
  def loadFile(file: String): Setup = {
    val r = Source.fromFile(file)
    val json = r.mkString
    r.close()
    load(json)
  }

  def empty = new Setup(None, None)
}

case class Setup(var videoPath: Option[String],
                 var gpsPath: Option[String]) {

  def save = Setup.save(this)
  def saveFile(path: String) = Setup.saveFile(path, this)
}
