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
    val list = node.child
    list.filter(_.isInstanceOf[Elem]).foreach{item =>
      //println(item)
      println
      println("name="+item.label)
      println("text="+item.text)
    }
    empty
  }
}

case class GarminExtension(cadence: Option[Double],
                               temperature: Option[Double],
                               heartRate: Option[Double]) {
}
