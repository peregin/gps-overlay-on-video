package peregin.gpv.model

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime
import org.specs2.mutable.Specification


class TrackPointSpec extends Specification {

  "interpolate distance" in {
    val pos1 = TrackPoint(new GeoPosition(0, 0), 100, new DateTime(2014, 6, 1, 10, 0), GarminExtension(Some(72), Some(12), Some(110), None))
    val pos2 = TrackPoint(new GeoPosition(0, 1), 100, new DateTime(2014, 6, 1, 10, 0), GarminExtension(Some(72), Some(12), Some(110), None))
    val pos3 = TrackPoint(new GeoPosition(1, 0), 100, new DateTime(2014, 6, 1, 10, 0), GarminExtension(Some(72), Some(12), Some(110), None))
    pos1.bearingTo(pos2) === 90
    pos1.bearingTo(pos3) === 0
    pos2.bearingTo(pos1) === 270
    pos3.bearingTo(pos1) === 180
  }
}
