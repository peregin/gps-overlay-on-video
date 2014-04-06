package peregin.tov.gui

import scala.swing._
import peregin.tov.util.Logging
import org.jdesktop.swingx.JXMapKit
import org.jdesktop.swingx.mapviewer.GeoPosition
import java.io.File


class TelemetryPanel extends MigPanel("ins 2", "", "[fill]") with Logging {

  val chooser = new FileChooserPanel("Load GPS data file:", openGpsData)
  add(chooser, "pushx, growx, wrap")

  val mapKit = new JXMapKit
  mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps)
  mapKit.setAddressLocation(new GeoPosition(47.366074, 8.541264)) // Buerkliplatz
  add(Component.wrap(mapKit), "growx, wrap")

  def openGpsData(file: File) {

  }
}
