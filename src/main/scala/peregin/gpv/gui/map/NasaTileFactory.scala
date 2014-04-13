package peregin.gpv.gui.map

import org.jdesktop.swingx.mapviewer.wms.{WMSService, WMSTileFactory}


object NasaTileFactory {
  val wms = new WMSService()
  wms.setLayer("BMNG")
  wms.setBaseUrl("http://wms.jpl.nasa.gov/wms.cgi?")
}


class NasaTileFactory extends WMSTileFactory(NasaTileFactory.wms)
