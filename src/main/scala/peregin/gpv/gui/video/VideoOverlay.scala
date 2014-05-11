package peregin.gpv.gui.video

import peregin.gpv.model.Telemetry
import com.xuggle.mediatool.MediaToolAdapter
import java.awt.{AlphaComposite, Image}
import com.xuggle.mediatool.event.IVideoPictureEvent
import peregin.gpv.gui.gauge._
import peregin.gpv.util.Logging


class VideoOverlay(telemetry: Telemetry, imageHandler: Image => Unit) extends MediaToolAdapter with Logging {

  val speedGauge = new RadialSpeedGauge {}
  val cadenceGauge = new CadenceGauge {}
  val elevationGauge = new IconicElevationGauge {}
  val distanceGauge = new IconicDistanceGauge {}
  val heartRateGauge = new IconicHeartRateGauge {}
  val debug = true
  val debugGauge = if (debug) Some(new DebugGauge {}) else None

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val ts = event.getTimeStamp
    val unit = event.getTimeUnit
    val tsInMillis = unit.toMillis(ts)

    val image = event.getImage
    val g = image.createGraphics

    // set transparency
    g.setComposite(AlphaComposite.SrcOver.derive(0.5f))

    // TODO: apply the shift between video and gps streams
    telemetry.sonda(tsInMillis).foreach{sonda =>
      if (debugGauge.isDefined) sonda.videoProgress = tsInMillis
      val stash = g.getTransform

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

      // restore any kind of transformations until this point
      g.setTransform(stash)
      debugGauge.foreach{gauge =>
        // paint to bottom/right
        val w = image.getWidth
        val h = image.getHeight
        val debugBoxW = 250
        val debugBoxH = 150
        g.translate(w - debugBoxW, h - debugBoxH)
        gauge.paint(g, debugBoxW, debugBoxH, sonda)
      }
    }

    g.dispose()

    imageHandler(image)

    super.onVideoPicture(event)
  }
}
