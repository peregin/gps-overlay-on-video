package peregin.gpv.gui.video

import java.awt.Image

import com.xuggle.xuggler.video.ConverterFactory
import com.xuggle.xuggler._
import peregin.gpv.model.Telemetry

import scala.actors.{TIMEOUT, DaemonActor}


case class Seek(source: AnyRef, secs: Double)
case object Play
case object Stop
case object Dispose


class AnotherVideoPlayer(url: String, telemetry: Telemetry,
                         imageHandler: Image => Unit, shiftHandler: => Long,
                         timeUpdater: (Long, Int) => Unit) extends VideoPlayer {

  val container = IContainer.make()
  require( container.open( url, IContainer.Type.READ, null ) >= 0, "Could not open file: " + url )
  val numStreams = container.getNumStreams
  val (streamIdx, dec) = (0 until numStreams).map( i => i -> container.getStream( i ).getStreamCoder )
    .find( _._2.getCodecType == ICodec.Type.CODEC_TYPE_VIDEO )
    .getOrElse( sys.error( "Could not find video decoder for container: " + url ))

  require( dec.open() >= 0, "Could not open video decoder for container: "  + url )

  val pixType       = dec.getPixelType
  //      val convName      = "native_to_" + pixType.name
  val width         = dec.getWidth
  val height        = dec.getHeight
  val (conv, resampler: IVideoResampler) = pixType match {
    case IPixelFormat.Type.BGR24 =>
      ConverterFactory.createConverter( ConverterFactory.XUGGLER_BGR_24, pixType, width, height ) -> null
    case IPixelFormat.Type.ARGB  =>
      ConverterFactory.createConverter( ConverterFactory.XUGGLER_ARGB_32, pixType, width, height ) -> null
    case _ =>
      val res = IVideoResampler.make( width, height, IPixelFormat.Type.BGR24, width, height, pixType )
      require( res != null, "Could not create color space resampler for " + url )
      ConverterFactory.createConverter( ConverterFactory.XUGGLER_BGR_24, IPixelFormat.Type.BGR24, width, height ) -> res
  }

  @volatile private var timeViewVar = (source: AnyRef, secs: Double, playing: Boolean) => ()

  def timeView = timeViewVar
  def timeView_=( fun: (AnyRef, Double, Boolean) => Unit ) { timeViewVar = fun }

  private val stream    = container.getStream( streamIdx )
  private val timeBase  = stream.getTimeBase.getDouble

  //println( " TIME BASE = " + timeBase + " dur = " + stream.getDuration )

  protected lazy val actor = new DaemonActor {
    def act() {
      var time    = 0L
      val packet  = IPacket.make()
      val picIn   = IVideoPicture.make( dec.getPixelType, width, height )
      val picOut  = if( resampler == null ) picIn else
        IVideoPicture.make( resampler.getOutputPixelFormat, width, height )

      var vidCurrent = Global.NO_PTS

      def tryRead() : Boolean = {
        (container.readNextPacket( packet ) >= 0 && (packet.getStreamIndex == streamIdx)) && {
          var offset  = 0
          var picDone = false
          val pSize   = packet.getSize
          while( !picDone && offset < pSize ) {
            val num = dec.decodeVideo( picIn, packet, 0 )
            if( num >= 0 ) {
              offset  += num
              picDone  = picIn.isComplete
            } else {
              offset   = pSize // error -- break loop
            }
          }
          val succ = picDone && (resampler == null) || {
            resampler.resample( picOut, picIn ) >= 0 && picOut.getPixelType == IPixelFormat.Type.BGR24
          }
          if( succ ) {
            vidCurrent = picIn.getTimeStamp   // WARNING : this is microseconds, not re timebase !!!
          }
          succ
        }
      }

      def aDisplay( source: AnyRef, secs: Double, playing: Boolean ) {
        val image = conv.toImage( picOut )
        imageHandler(image)
        //            val tv = timeViewVar
        //            if( tv != null ) {
        ////println( Util.formatTimeString( secs ))
        //               tv.text = Util.formatTimeString( secs )
        //            }
        timeViewVar( source, secs, playing )
      }

      def aSeek( source: AnyRef, secs: Double ) : Boolean = {
        val succ = aSeekNoDisplay( secs )
        if ( succ ) aDisplay( source, secs, false )
        succ
      }

      def aSeekNoDisplay( secs: Double ) : Boolean = {
        time = (secs / timeBase).toLong
        container.seekKeyFrame( streamIdx, time, IContainer.SEEK_FLAG_BACKWARDS ) // .SEEK_FLAG_ANY )
        tryRead()
      }

      var open = true

      def aDispose() {
        try {
          dec.close()
          container.close()
        } finally {
          open = false
        }
      }

      loopWhile( open ) { react {
        case Seek( source, secs ) => aSeek( source, secs )

        case Play =>
          if( vidCurrent != Global.NO_PTS || aSeekNoDisplay( 0.0 )) {
            var playing    = true
            var delay      = 0L
            val sysStart   = System.currentTimeMillis()
            val vidStart   = vidCurrent

            def displayCurrent() {
              //                     aDisplay( (vidCurrent - vidStart) * 1.0e-6, true )
              aDisplay( sender, vidCurrent * 1.0e-6, true )  // XXX assumes stream begins at zero!
            }

            loopWhile( playing ) { reactWithin( delay ) {
              case TIMEOUT =>
                displayCurrent()
                playing = tryRead()
                if( playing ) {
                  val sysCurrent = System.currentTimeMillis()
                  val sysMillis  = sysCurrent - sysStart
                  //println( "dv " + (vidCurrent - vidStart) )
                  val vidMillis = ((vidCurrent - vidStart) * 1.0e-3).toLong
                  delay = math.max( 0L, vidMillis - sysMillis )
                }

              case Stop =>
                displayCurrent()
                playing = false
              case Seek( source, secs ) =>
                playing = false
                aSeek( source, secs )
              case Dispose =>
                playing = false
                aDispose()
            }}
          }

        case Stop =>
        case Dispose => aDispose()
      }}
    }
  }

  actor.start()
  actor ! Play

  override def seek(percentage: Double) = actor ! Seek(this, percentage)

  override def close() = actor ! Stop
}
