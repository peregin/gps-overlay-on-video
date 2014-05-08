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

  override def onVideoPicture(event: IVideoPictureEvent) = {
    val ts = event.getTimeStamp
    val unit = event.getTimeUnit
    val tsInMillis = unit.toMillis(ts)
    //log.debug(s"mill = $tsInMillis, ts = $ts, unit = $unit")

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

    imageHandler(image)

    super.onVideoPicture(event)
  }
}
