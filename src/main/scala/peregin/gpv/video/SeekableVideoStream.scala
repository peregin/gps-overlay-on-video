package peregin.gpv.video

import org.bytedeco.javacv.{FFmpegFrameGrabber, Frame, Java2DFrameConverter}

import java.awt.image.BufferedImage
import peregin.gpv.util.{Logging, TimePrinter}


sealed trait PacketReply
case object ReadInProgress extends PacketReply
case object EndOfStream extends PacketReply
case class FrameIsReady(tsInMillis: Long, percentage: Double, keyFrame: Boolean, image: BufferedImage, rotation: Double) extends PacketReply

class SeekableVideoStream(url: String) extends DelayController with Logging {

  val grabber: FFmpegFrameGrabber = new FFmpegFrameGrabber(url);

  var imageType: Int = -1;
  var image: BufferedImage = _

  grabber.start()

  val durationInMillis = grabber.getLengthInTime / 1000L
  info(s"duration: ${TimePrinter.printDuration(durationInMillis)}")

  if (!grabber.hasVideo) {
    throw new IllegalArgumentException("Video stream not found in: " + url);
  }

  // and iterate through the streams to find the first video stream
  val frameRate = grabber.getVideoFrameRate

  info(f"frame rate: $frameRate%5.2f")

  final def readNextFrame: Option[FrameIsReady] = {
    val frame: Frame = grabber.grabImage()
    if (frame == null) {
      return None
    }
    if (Java2DFrameConverter.getBufferedImageType(frame) != imageType) {
      imageType = Java2DFrameConverter.getBufferedImageType(frame)
      image = new BufferedImage(frame.imageWidth, frame.imageHeight, imageType)
    }
    Java2DFrameConverter.copy(frame, image)
    return Some(FrameIsReady(frame.timestamp / 1000, frame.timestamp * (100.0 / 1000.0) / durationInMillis, frame.keyFrame, image, grabber.getDisplayRotation))
  }


  def seek(percentage: Double): Option[FrameIsReady] = {
    val p = percentage match {
      case a if a > 100d => 100d
      case b if b < 0d => 0d
      case _ => percentage
    }

    val jumpToMillis = p * durationInMillis / 100

    grabber.setVideoTimestamp((jumpToMillis * 1000).toLong)
    log.info(f"seek to $p%2.2f percentage, jumpToSecond = ${TimePrinter.printDuration((jumpToMillis).toLong)} out of ${TimePrinter.printDuration(durationInMillis)}")

    readNextFrame
  }

  def close(): Unit = {
    grabber.close()
  }
}
