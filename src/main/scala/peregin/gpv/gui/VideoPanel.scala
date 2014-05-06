package peregin.gpv.gui

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import com.xuggle.mediatool.{IMediaReader, MediaToolAdapter, ToolFactory}
import java.awt.image.BufferedImage
import com.xuggle.mediatool.event.IVideoPictureEvent
import javax.swing.{JSlider, JPanel}
import java.awt.{AlphaComposite, Color, Graphics, Image}
import peregin.gpv.Setup
import peregin.gpv.util.Logging
import scala.swing.Swing
import scala.concurrent._
import peregin.gpv.gui.gauge._
import peregin.gpv.model.Telemetry
import scala.Some


class VideoPanel(openVideoData: File => Unit) extends MigPanel("ins 2", "", "[fill]") with Logging {

  var telemetry = Telemetry.empty

  val chooser = new FileChooserPanel("Load video file:", openVideoData, new FileNameExtensionFilter("Video files (mp4)", "mp4"))
  add(chooser, "pushx, growx, wrap")

  class ImagePanel extends JPanel {
    var image: Image = null

    def show(im: Image) {
      image = im
      repaint()
    }

    override def paint(g: Graphics) = {
      g.setColor(Color.black)
      val width = getWidth
      val height = getHeight
      g.fillRect(0, 0, width, height)

      if (image != null) {
        val w = image.getWidth(null)
        val h = image.getHeight(null)
        val x = (width - w) / 2
        val y = (height - h) / 2
        g.drawImage(image, x, y, w, h, null)
      }
    }
  }
  val imagePanel = new ImagePanel
  add(imagePanel, "grow, pushy, wrap")

  val controlPanel = new MigPanel("ins 0", "", "") {
    add(new JSlider(0, 100, 0), "pushx, growx")
    add(new ImageButton("images/play.png", "Play", playOrPauseVideo()), "align right")
  }
  add(controlPanel, "growx")

  @volatile var reader: Option[IMediaReader] = None

  def refresh(setup: Setup, telemetry: Telemetry) {
    chooser.fileInput.text = setup.videoPath.getOrElse("")
    this.telemetry = telemetry

    setup.videoPath.foreach{path =>
      synchronized {

        val speedGauge = new RadialSpeedGauge {}
        val cadenceGauge = new CadenceGauge {}
        val elevationGauge = new IconicElevationGauge {}
        val distanceGauge = new IconicDistanceGauge {}
        val heartRateGauge = new HeartRateGauge {}

        reader.foreach {
          mr => if (mr.isOpen) mr.close()
        }
        reader = Some(ToolFactory.makeReader(path))
        reader.foreach{mr =>
          mr.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR)
          mr.addListener(new MediaToolAdapter {
            override def onVideoPicture(event: IVideoPictureEvent) = {
              val ts = event.getTimeStamp
              val unit = event.getTimeUnit
              val tsInMillis = unit.toMillis(ts)
              log.debug(s"mill = $tsInMillis, ts = $ts, unit = $unit")

              val image = event.getImage
              val g = image.createGraphics

              // set transparency
              g.setComposite(AlphaComposite.SrcOver.derive(0.5f))

              telemetry.sonda(tsInMillis).foreach{sonda =>
                speedGauge.paint(g, 75, 75, sonda)
                if (sonda.cadence.isDefined) {
                  g.translate(75, 0)
                  cadenceGauge.paint(g, 75, 75, sonda)
                }
                g.translate(75, 0)
                elevationGauge.paint(g, 75, 75, sonda)
                g.translate(75, 0)
                distanceGauge.paint(g, 75, 75, sonda)
                if (sonda.heartRate.isDefined) {
                  g.translate(75, 0)
                  heartRateGauge.paint(g, 75, 75, sonda)
                }
              }

              g.dispose()

              Swing.onEDT(imagePanel.show(image))

              super.onVideoPicture(event)
            }
          })
        import ExecutionContext.Implicits.global
          future {
            (0 until 200).foreach{_ =>
              mr.readPacket()
              Thread.sleep(30)
            }
          }
        }
      }
    }
  }

  def playOrPauseVideo() {
    log.info("play video...")
  }
}
