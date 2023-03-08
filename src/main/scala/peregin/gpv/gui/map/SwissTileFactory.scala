package peregin.gpv.gui.map

import org.jdesktop.swingx.mapviewer.wms.{WMSService, WMSTileFactory}


object SwissTileFactory {
  val wms = new WMSService()
  wms.setLayer("BMNG")
  wms.setBaseUrl("https://wms.geo.admin.ch/")
}


class SwissTileFactory extends WMSTileFactory(SwissTileFactory.wms)
