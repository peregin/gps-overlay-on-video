package peregin.gpv.gui.video

import akka.actor.{Props, ActorSystem, Actor}
import com.xuggle.xuggler._

import peregin.gpv.model.Telemetry
import java.awt.Image
import peregin.gpv.util.{TimePrinter, Logging}

import scala.annotation.tailrec


trait ExperimentalVideoPlayerFactory extends VideoPlayerFactory {
  override def createPlayer(url: String, telemetry: Telemetry, imageHandler: Image => Unit,
                            shiftHandler: => Long, timeUpdater: (Long, Double) => Unit) =
    new ExperimentalVideoPlayer(url, telemetry, imageHandler, shiftHandler, timeUpdater)
}

sealed trait PacketReply
case object ReadInProgress extends PacketReply
case object EndOfStream extends PacketReply
case class FrameIsReady(tsInMillis: Long, percentage: Double, keyFrame: Boolean, image: Image) extends PacketReply

// migration of the xuggler sample
class ExperimentalVideoPlayer(url: String, telemetry: Telemetry,
                        val imageHandler: Image => Unit, shiftHandler: => Long,
                        val timeUpdater: (Long, Double) => Unit) extends VideoPlayer with DelayController with Logging {


  // Let's make sure that we can actually convert video pixel formats.
  if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
    throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support)")

  // Create a Xuggler container object
  val container = IContainer.make()

  // Open up the container
  if (container.open(url, IContainer.Type.READ, null) < 0)
    throw new IllegalArgumentException(s"could not open file: $url")

  val durationInMillis = container.getDuration / 1000
  info(s"duration: ${TimePrinter.printDuration(durationInMillis)}")

  // query how many streams the call to open found
  val numStreams = container.getNumStreams()

  // and iterate through the streams to find the first video stream
  var videoStreamId = -1
  var frameRate = 0d
  var timeBase = 1d

  var videoCoder: IStreamCoder = null
  for (i <- 0 until numStreams) {
    // Find the stream object
    val stream = container.getStream(i)
    // Get the pre-configured decoder that can decode this stream
    val coder = stream.getStreamCoder()

    if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
      videoStreamId = i
      videoCoder = coder
      frameRate = videoCoder.getFrameRate.getDouble
      info(f"frame rate: $frameRate%5.2f")
      timeBase = 1d / stream.getTimeBase.getDouble
      info(f"time base: $timeBase%10.2f")
    }
  }
  if (videoStreamId == -1) throw new RuntimeException(s"could not find video stream in container: $url")

  /*
   * Now we have found the video stream in this file.  Let's open up our decoder so it can
   * do work.
   */
  if (videoCoder.open() < 0)
    throw new RuntimeException(s"could not open video decoder for container: $url")

  var resampler: IVideoResampler = null
  if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
    // if this stream is not in BGR24, we're going to need to
    // convert it.  The VideoResampler does that for us.
    resampler = IVideoResampler.make(videoCoder.getWidth(),
      videoCoder.getHeight(), IPixelFormat.Type.BGR24,
      videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType())
    if (resampler == null) throw new RuntimeException(s"could not create color space resampler for: $url")
  }

  val packet = IPacket.make()

  def readPacket: Stream[PacketReply] = {
    if (container.readNextPacket(packet) >= 0) {
      if (packet.getStreamIndex() == videoStreamId) Stream.cons(readVideo, readPacket)
      else Stream.cons(ReadInProgress, readPacket)
    } else Stream.cons(EndOfStream, Stream.empty)
  }

  private def readVideo: PacketReply = {
    // We allocate a new picture to get the data out of Xuggler
    val picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight())
    var offset = 0
    while (offset < packet.getSize() && !picture.isComplete) {
      // Now, we decode the video, checking for any errors.
      val bytesDecoded = videoCoder.decodeVideo(picture, packet, offset)
      if (bytesDecoded < 0) throw new RuntimeException(s"got error decoding video in: $url")
      offset += bytesDecoded
    } // offset less than packet size

    /*
     * Some decoders will consume data in a packet, but will not be able to construct
     * a full video picture yet.  Therefore you should always check if you got a complete picture from the decoder
     */
    if (picture.isComplete()) {
      var newPic: IVideoPicture = picture
      // If the resampler is not null, that means we didn't get the video in BGR24 format and  need to convert it into BGR24 format.
      if (resampler != null) {
        // we must resample
        newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight())
        if (resampler.resample(newPic, picture) < 0) throw new RuntimeException(s"could not resample video from: $url")
      }
      if (newPic.getPixelType() != IPixelFormat.Type.BGR24) throw new RuntimeException(s"could not decode video as BGR 24 bit data in: $url")

      // remember that IVideoPicture and IAudioSamples timestamps are always in MICROSECONDS, so we divide by 1000 to get milliseconds.
      val tsInMillis = picture.getTimeStamp / 1000
      val percentage = if (durationInMillis > 0) (tsInMillis * 100).toDouble / durationInMillis else 0
      // And finally, convert the BGR24 to an Java buffered image
      val javaImage = Utils.videoPictureToImage(newPic)

      FrameIsReady(tsInMillis, percentage, picture.isKeyFrame, javaImage)
    } else ReadInProgress
  }

  @tailrec
  private def readNextKeyFrame: Option[FrameIsReady] = {
    readPacket.head match {
      case keyFrame @ FrameIsReady(_, _, true, _) => Some(keyFrame)
      case EndOfStream => None
      case _ => readNextKeyFrame
    }
  }

  @tailrec
  private def readFrameUntilTimestamp(maxTsInMillis: Long): Option[FrameIsReady] = {
    readPacket.head match {
      case frame @ FrameIsReady(frameTsInMillis, _, _, _) if frameTsInMillis >= maxTsInMillis => Some(frame)
      case EndOfStream => None
      case _ => readFrameUntilTimestamp(maxTsInMillis)
    }
  }

  override def play() {
    playerActor ! Play
  }

  override def pause() = sys.error("not supported")

  override def seek(percentage: Double) {
    playerActor ! Seek(percentage)
  }

  // See: http://wiki.xuggle.com/Concepts#Time_Bases
  private def seconds2Timebase(s: Double): Long = (s * timeBase).toLong

  private[video] def doSeek(percentage: Double): Option[FrameIsReady] = {
    val p = percentage match {
      case a if a > 100d => 100d
      case b if b < 0d => 0d
      case c => percentage
    }

    val jumpToMillis = p * durationInMillis / 100
    val jumpToSecond = jumpToMillis / 1000
    log.info(f"seek to $p%2.2f percentage, jumpToSecond = ${TimePrinter.printDuration((jumpToSecond * 1000).toLong)} out of ${TimePrinter.printDuration(durationInMillis)}")
    val pos = seconds2Timebase(jumpToSecond)
    container.seekKeyFrame(videoStreamId, pos - 100, pos, pos, IContainer.SEEK_FLAG_FRAME)

    // jump to closest key fame
    val keyFrameOption = readNextKeyFrame

    // loop to closest timestamp
    val closestFrameOption = readFrameUntilTimestamp(jumpToMillis.toLong)

    // reset timer
    reset()

    closestFrameOption orElse keyFrameOption
  }

  override def close() {
    if (videoCoder != null) {
      videoCoder.close()
    }
    if (container != null) {
      container.close()
    }
  }

  val system = ActorSystem("gpv")
  val playerActor = system.actorOf(Props(new PlayerControllerActor(this)), name = "playerController")
  playerActor ! Play
}

sealed trait PlayerCommand
case object Play extends PlayerCommand
case class Seek(percentage: Double) extends PlayerCommand

class PlayerControllerActor(player: ExperimentalVideoPlayer) extends Actor with Logging {

  override def receive = {
    case Play => player.readPacket.head match {
      case FrameIsReady(tsInMillis, percentage, keyFrame, image) =>
        info(f"frame received, ts=${TimePrinter.printDuration(tsInMillis)}, @=$percentage%2.2f, keyFrame=$keyFrame")
        if (tsInMillis > 0) player.waitIfNeeded(tsInMillis)
        player.timeUpdater(tsInMillis, percentage)
        player.imageHandler(image)
        //self ! Play
      case EndOfStream => context.stop(self)
      case ReadInProgress => self ! Play
      case _ => // ignore
    }
    case Seek(percentage) =>
      player.doSeek(percentage).foreach{ seekFrame =>
        info(f"seek frame received, ts=${TimePrinter.printDuration(seekFrame.tsInMillis)}, @=${seekFrame.percentage%2.2f}")
        player.timeUpdater(seekFrame.tsInMillis, seekFrame.percentage)
        player.imageHandler(seekFrame.image)
      }
    case _ => // do nothing
  }
}
