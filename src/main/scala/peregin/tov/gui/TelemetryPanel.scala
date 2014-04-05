package peregin.tov.gui

import scala.swing._
import scala.swing.event.ButtonClicked
import peregin.tov.App
import peregin.tov.util.Logging
import org.jdesktop.swingx.JXMapKit
import scala.swing.event.ButtonClicked


class TelemetryPanel extends MigPanel("", "", "[fill]") with Logging {

  add(new Label("GPS data file:"), "span 2, wrap")
  val browseButton = new Button("Browse")
  add(browseButton, "")
  val fileInput = new TextArea("")
  add(fileInput, "pushx, growx, wrap")

  val mapKit = new JXMapKit
  mapKit.setDefaultProvider(JXMapKit.DefaultProviders.OpenStreetMaps)
  add(Component.wrap(mapKit), "span 2, growx, wrap")


  listenTo(browseButton)
  reactions += {
    case ButtonClicked(`browseButton`) => openData
  }

  def openData = {
    val chooser = new FileChooser()
    if (chooser.showOpenDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"opening ${file.getAbsolutePath}")
      fileInput.text = file.getAbsolutePath
    }
  }
}
