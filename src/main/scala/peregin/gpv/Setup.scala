package peregin.gpv

import java.io.FileWriter
import peregin.gpv.util.JsonConverter
import scala.io.Source

object Setup {

  def save(setup: Setup): String = JsonConverter.generate(setup)
  def saveFile(file: String, setup: Setup): Unit = {
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

  def empty() = new Setup(None, None, None, None, None, None, None, None)
}

case class Setup(var videoPath: Option[String],
                 var gpsPath: Option[String],
                 var outputPath: Option[String],
                 var shiftTimestamp: Option[Long],
                 var dashboardTransparency: Option[Double],
                 var dashboardUnits: Option[String],
                 var dashboardCode: Option[String],
                 var bitrateRatio: Option[Int]
                ) {

  def save: String = Setup.save(this)
  def saveFile(path: String): Unit = Setup.saveFile(path, this)

  // in millis
  def shift: Long = shiftTimestamp.getOrElse(0L)
  def shift_= (value: Long): Unit = shiftTimestamp = Some(value)

  // in percentage
  def transparency: Double = dashboardTransparency.getOrElse(60d)
  def transparency_=(value: Double): Unit = dashboardTransparency = Some(value)

  // units
  def units: String = dashboardUnits.getOrElse("Metric")
  def units_=(value: String): Unit = dashboardUnits = Some(value)
}
