package peregin.gpv.gui.map

import org.jdesktop.swingx.mapviewer.{DefaultTileFactory, TileFactoryInfo}

// open street map
class MapQuestTileFactory extends DefaultTileFactory(new MapQuestTileInfo)

class MapQuestTileInfo extends TileFactoryInfo(
  0, // Minimum zoom level
  17, // Maximum zoom level
  2, // Total zoom level count
  256, // Tile size in pixels
  true, // X axis is tiled
  true, // Y axis is tiled
  "https://otile1.mqcdn.com/tiles/1.0.0/osm/", // Base tile URL
  "x", "y", "z" // Tile URL parameters
);
