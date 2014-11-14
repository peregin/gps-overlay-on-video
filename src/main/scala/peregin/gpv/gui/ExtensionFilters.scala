package peregin.gpv.gui

import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Created by peregin on 14/11/14.
 */
object ExtensionFilters {

  val video = new FileNameExtensionFilter("Video files (mp4)", "mp4")
  val project = new FileNameExtensionFilter("project file (json)", "json")
  val gps = new FileNameExtensionFilter("GPS files (gpx)", "gpx")
}
