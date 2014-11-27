package peregin.gpv.gui.map

import org.jdesktop.swingx.mapviewer.{DefaultTileFactory, TileFactoryInfo}


class MicrosoftTileFactory extends DefaultTileFactory(new MicrosoftTileFactoryInfo)

object MicrosoftTileFactoryInfo {
  val VERSION = "174"
  val minZoom = 1
  val maxZoom = 16
  val mapZoom = 17
  val tileSize = 256
  val xr2l = true
  val yt2b = true
  val baseURL = "http://a2.ortho.tiles.virtualearth.net/tiles/a"
}

import MicrosoftTileFactoryInfo._
class MicrosoftTileFactoryInfo extends TileFactoryInfo(minZoom, maxZoom, mapZoom, tileSize, xr2l, yt2b, baseURL, null, null, null) {

  override def getTileUrl(x: Int, y: Int, zoom: Int) = {
    baseURL + xyzoom2quadrants(x, y, zoom) + ".jpeg?g=" + VERSION
  }

  def xyzoom2quadrants(x: Int, y: Int, zoom: Int) = {
    val quad = new StringBuffer()
    var level = 1 << (maxZoom - zoom)
    var tx = x
    var ty = y
    while (level > 0) {
      var ix = 0
      if (tx >= level) {
        ix += 1
        tx -= level
      }
      if (ty >= level) {
        ix += 2
        ty -= level
      }
      quad.append(ix)
      level /= 2
    }
    quad.toString
  }

}
