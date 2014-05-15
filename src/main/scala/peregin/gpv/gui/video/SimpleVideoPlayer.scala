package peregin.gpv.gui.video

import com.xuggle.xuggler._

import peregin.gpv.model.Telemetry
import java.awt.Image
import peregin.gpv.util.{DurationPrinter, Logging}


// migration of the xuggler sample
class SimpleVideoPlayer(url: String, telemetry: Telemetry,
                        imageHandler: Image => Unit, shiftHandler: => Long,
                        timeUpdater: (Long, Int) => Unit) extends Logging {


  // Let's make sure that we can actually convert video pixel formats.
  if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
    throw new RuntimeException("you must install the GPL version of Xuggler (with IVideoResampler support) ")

  // Create a Xuggler container object
  val container = IContainer.make()

  // Open up the container
  if (container.open(url, IContainer.Type.READ, null) < 0)
    throw new IllegalArgumentException("could not open file: " + url)

  val durationInMillis = container.getDuration / 1000
  info(s"duration: ${DurationPrinter.print(durationInMillis)}")

  // query how many streams the call to open found
  val numStreams = container.getNumStreams()

  // and iterate through the streams to find the first video stream
  var videoStreamId = -1
  var videoCoder: IStreamCoder = null
  for (i <- 0 until numStreams) {
    // Find the stream object
    val stream = container.getStream(i)
    // Get the pre-configured decoder that can decode this stream;
    val coder = stream.getStreamCoder()

    if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
      videoStreamId = i
      videoCoder = coder
      val frameRate = videoCoder.getFrameRate.getDouble
      info(f"frame rate: $frameRate%5.2f")
    }
  }
  if (videoStreamId == -1)
    throw new RuntimeException("could not find video stream in container: " + url)

  /*
   * Now we have found the video stream in this file.  Let's open up our decoder so it can
   * do work.
   */
  if (videoCoder.open() < 0)
    throw new RuntimeException("could not open video decoder for container: " + url)

  var resampler: IVideoResampler = null
  if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
    // if this stream is not in BGR24, we're going to need to
    // convert it.  The VideoResampler does that for us.
    resampler = IVideoResampler.make(videoCoder.getWidth(),
      videoCoder.getHeight(), IPixelFormat.Type.BGR24,
      videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType())
    if (resampler == null)
      throw new RuntimeException("could not create color space " +
        "resampler for: " + url)
  }

  import scala.concurrent._
  import ExecutionContext.Implicits.global
  future {
    run()
  }

  def run() {
    /*
     * Now, we start walking through the container looking at each packet.
     */
    val packet = IPacket.make()
    var firstTimestampInStream = Global.NO_PTS
    var systemClockStartTime = 0L
    while (container.readNextPacket(packet) >= 0) {
      /*
     * Now we have a packet, let's see if it belongs to our video stream
     */
      if (packet.getStreamIndex() == videoStreamId) {
        /*
       * We allocate a new picture to get the data out of Xuggler
       */
        val picture = IVideoPicture.make(videoCoder.getPixelType(),
          videoCoder.getWidth(), videoCoder.getHeight())

        var offset = 0
        while (offset < packet.getSize()) {
          /*
         * Now, we decode the video, checking for any errors.
         *
         */
          val bytesDecoded = videoCoder.decodeVideo(picture, packet, offset)
          if (bytesDecoded < 0)
            throw new RuntimeException("got error decoding video in: " + url);
          offset += bytesDecoded;

          /*
         * Some decoders will consume data in a packet, but will not be able to construct
         * a full video picture yet.  Therefore you should always check if you
         * got a complete picture from the decoder
         */
          if (picture.isComplete()) {
            var newPic: IVideoPicture = picture
            /*
           * If the resampler is not null, that means we didn't get the
           * video in BGR24 format and
           * need to convert it into BGR24 format.
           */
            if (resampler != null) {
              // we must resample
              newPic = IVideoPicture.make(resampler.getOutputPixelFormat(),
                picture.getWidth(), picture.getHeight());
              if (resampler.resample(newPic, picture) < 0)
                throw new RuntimeException("could not resample video from: "
                  + url);
            }
            if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
              throw new RuntimeException("could not decode video" +
                " as BGR 24 bit data in: " + url);

            /**
             * We could just display the images as quickly as we decode them,
             * but it turns out we can decode a lot faster than you think.
             *
             * So instead, the following code does a poor-man's version of
             * trying to match up the frame-rate requested for each
             * IVideoPicture with the system clock time on your computer.
             *
             * Remember that all Xuggler IAudioSamples and IVideoPicture objects
             * always give timestamps in Microseconds, relative to the first
             * decoded item. If instead you used the packet timestamps, they can
             * be in different units depending on your IContainer, and IStream
             * and things can get hairy quickly.
             */
            if (firstTimestampInStream == Global.NO_PTS) {
              // This is our first time through
              firstTimestampInStream = picture.getTimeStamp();
              // get the starting clock time so we can hold up frames
              // until the right time.
              systemClockStartTime = System.currentTimeMillis();
            } else {
              val systemClockCurrentTime = System.currentTimeMillis();
              val millisecondsClockTimeSinceStartofVideo =
                systemClockCurrentTime - systemClockStartTime;
              // compute how long for this frame since the first frame in the
              // stream.
              // remember that IVideoPicture and IAudioSamples timestamps are
              // always in MICROSECONDS,
              // so we divide by 1000 to get milliseconds.
              val millisecondsStreamTimeSinceStartOfVideo =
                (picture.getTimeStamp() - firstTimestampInStream) / 1000;
              val millisecondsTolerance = 50L // and we give ourselfs 50 ms of tolerance
              val millisecondsToSleep =
                (millisecondsStreamTimeSinceStartOfVideo -
                  (millisecondsClockTimeSinceStartofVideo +
                    millisecondsTolerance));
              if (millisecondsToSleep > 0) {
                Thread.sleep(millisecondsToSleep);
              }
            }

            // And finally, convert the BGR24 to an Java buffered image
            val javaImage = Utils.videoPictureToImage(newPic)

            // and display it on the Java Swing window
            imageHandler(javaImage)
          }
        }
      }
      else {
        /*
       * This packet isn't part of our video stream, so we just
       * silently drop it.
       */
        do {} while (false);
      }

    }
    /*
   * Technically since we're exiting anyway, these will be cleaned up by
   * the garbage collector... but because we're nice people and want
   * to be invited places for Christmas, we're going to show how to clean up.
   */
    if (videoCoder != null) {
      videoCoder.close();
    }
    if (container != null) {
      container.close();
    }
  }
}
