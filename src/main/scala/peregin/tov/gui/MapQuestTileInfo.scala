package peregin.tov.gui

import org.jdesktop.swingx.mapviewer.TileFactoryInfo

object MapQuestTileInfo {
  val MAX_ZOOM = 17
}

class MapQuestTileInfo extends TileFactoryInfo(1,
  MapQuestTileInfo.MAX_ZOOM - 2, MapQuestTileInfo.MAX_ZOOM, 256, true, true,
  "http://otile1.mqcdn.com/tiles/1.0.0/osm",
  "x", "y", "z") {

  override def getTileUrl(x: Int, y: Int, zoom: Int): String = {
    val z = MapQuestTileInfo.MAX_ZOOM - zoom;
    this.baseURL + "/" + z + "/" + x + "/" + y + ".png"
  }

}
