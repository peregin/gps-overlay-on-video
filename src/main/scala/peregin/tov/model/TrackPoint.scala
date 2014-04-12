package peregin.tov.model

import org.jdesktop.swingx.mapviewer.GeoPosition
import org.joda.time.DateTime

case class TrackPoint(
  position: GeoPosition, elevation: Double,
  time: DateTime
)
