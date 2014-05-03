package peregin.gpv.model

import peregin.gpv.util.Logging
import scala.xml.{Elem, XML}

/**
 * GPX file extensions are device specific, this one is Garmin specific, the schema is available form Garmin
 * <code><pre>
 *   <gpxtpx:TrackPointExtension xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3"
 *     xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1"
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/1">
 *     <gpxtpx:atemp>14</gpxtpx:atemp>
 *     <gpxtpx:hr>110</gpxtpx:hr>
 *     <gpxtpx:cad>59</gpxtpx:cad>
 *   </gpxtpx:TrackPointExtension>
 * </pre></code>
 */
object GarminExtension extends Logging {

  def empty = new GarminExtension(None, None, None)

  def parse(xml: String): GarminExtension = {
    //log.info(s"parsing extension $xml")
    val node = XML.loadString(xml)
    //val binding = scalaxb.fromXML[TrackPointExtension_t](node)
    val map = collection.mutable.HashMap[String, String]()
    val list = node.child
    list.filter(_.isInstanceOf[Elem]).foreach{item =>
      map += item.label -> item.text
    }
    import scala.util.control.Exception._
    def convert2Double(s: String): Option[Double] = catching(classOf[NumberFormatException]) opt s.toDouble
    val cadence = map.get("cad").flatMap(convert2Double)
    val temperature = map.get("atemp").flatMap(convert2Double)
    val heartRate = map.get("hr").flatMap(convert2Double)
    new GarminExtension(cadence, temperature, heartRate)
  }
}

case class GarminExtension(cadence: Option[Double],
                           temperature: Option[Double],
                           heartRate: Option[Double]) {
}
