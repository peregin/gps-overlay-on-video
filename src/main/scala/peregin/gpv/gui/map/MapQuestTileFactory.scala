package peregin.gpv.gui.map

import org.jdesktop.swingx.mapviewer.{DefaultTileFactory, TileFactoryInfo}

// open street map
class MapQuestTileFactory extends DefaultTileFactory(new MapQuestTileInfo)

object MapQuestTileInfo {
  val maxZoom = 17
}
class MapQuestTileInfo extends TileFactoryInfo(1,
  MapQuestTileInfo.maxZoom - 2, MapQuestTileInfo.maxZoom, 256, true, true,
  "http://otile1.mqcdn.com/tiles/1.0.0/osm",
  "x", "y", "z") {

  override def getTileUrl(x: Int, y: Int, zoom: Int): String = {
    val z = MapQuestTileInfo.maxZoom - zoom
    s"${this.baseURL}/$z/$x/$y.png"
  }

}
