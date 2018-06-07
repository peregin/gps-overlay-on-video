package peregin.gpv.model

import peregin.gpv.util.Logging

import scala.xml.NodeSeq

/**
 * GPX file extensions are device specific, this one is Garmin specific, the schema is available form Garmin
 * <code><pre>
  *
 *   <extensions>
 *     <power>205</power>
 *     <gpxtpx:TrackPointExtension>
 *       <gpxtpx:atemp>8</gpxtpx:atemp>
 *       <gpxtpx:hr>160</gpxtpx:hr>
 *       <gpxtpx:cad>90</gpxtpx:cad>
 *     </gpxtpx:TrackPointExtension>
 *   </extensions>
  *
 * </pre></code>
 */
object GarminExtension extends Logging {

  def empty = new GarminExtension(None, None, None, None)

  def parse(node: NodeSeq): GarminExtension = {
    import scala.util.control.Exception._
    implicit def convert2Double(s: String): Option[Double] = catching(classOf[NumberFormatException]) opt s.toDouble

    val cadence = (node \ "TrackPointExtension" \ "cad").text
    val temperature = (node \ "TrackPointExtension" \ "atemp").text
    val heartRate = (node \ "TrackPointExtension" \ "hr").text
    val power = (node \ "power").text

    new GarminExtension(cadence, temperature, heartRate, power)
  }
}

case class GarminExtension(cadence: Option[Double],
                           temperature: Option[Double],
                           heartRate: Option[Double],
                           power: Option[Double])
