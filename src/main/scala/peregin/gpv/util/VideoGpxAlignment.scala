package peregin.gpv.util

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.joda.time.DateTime
import peregin.gpv.model.Telemetry

import scala.util.Try

/**
 * Computes the time shift (in millis) to align video with GPX so that
 * video time 0 corresponds to the correct moment in the track.
 *
 * Strategy:
 * 1. If the video has creation_time (or similar) metadata, use it so that
 *    video start aligns with that real-world time in the GPX.
 * 2. Otherwise assume the video is the end of the ride: align video end with GPX end
 *    (shift = gpxDuration - videoDuration).
 */
object VideoGpxAlignment extends Logging {

  private val CreationTimeKeys = Seq("creation_time", "creation-time", "creation time")

  /**
   * @param videoPath path to the video file
   * @param telemetry loaded GPX telemetry (must be non-empty)
   * @return suggested shift in millis: GPX time = video time + shift; None if alignment cannot be computed
   */
  def computeSuggestedShift(videoPath: String, telemetry: Telemetry): Option[Long] = {
    if (telemetry.track.isEmpty) {
      debug("telemetry is empty, cannot compute shift")
      return None
    }

    var grabber: FFmpegFrameGrabber = null
    try {
      grabber = new FFmpegFrameGrabber(videoPath)
      grabber.start()

      val videoDurationMs = grabber.getLengthInTime / 1000L
      val gpxStartMs = telemetry.minTime.getMillis
      val gpxEndMs = telemetry.maxTime.getMillis
      val gpxDurationMs = gpxEndMs - gpxStartMs

      // 1. Try metadata-based alignment (video recording start time)
      val shiftFromMetadata = readVideoStartTimeMs(grabber).map { videoStartEpochMs =>
        // We want: at video time 0, show GPX at videoStartEpochMs.
        // Lookup uses: track.head.time + (videoTs + shift) => we need gpxStartMs + (0 + shift) = videoStartEpochMs
        val shift = videoStartEpochMs - gpxStartMs
        info(s"video creation_time -> shift = ${shift}ms (align by metadata)")
        shift
      }

      if (shiftFromMetadata.isDefined) return shiftFromMetadata

      // 2. Fallback: align video start with GPX
      val shift = 0
      info(s"no video metadata -> shift = ${shift}ms (assuming start of video with beginning of GPX)")
      Some(shift)
    } catch {
      case ex: Exception =>
        log.error("Failed to compute video–GPX alignment", ex)
        None
    } finally {
      if (grabber != null) Try(grabber.close())
    }
  }

  private def readVideoStartTimeMs(grabber: FFmpegFrameGrabber): Option[Long] = {
    def valueFromMap(m: java.util.Map[String, String]): Option[String] =
      CreationTimeKeys.flatMap(k => Option(m.get(k))).headOption

    val fromFormat = Try(grabber.getMetadata()).toOption.filter(_ != null).flatMap(valueFromMap).flatMap(parseCreationTime)
    if (fromFormat.isDefined) return fromFormat

    Try(grabber.getVideoMetadata()).toOption.filter(_ != null).flatMap(valueFromMap).flatMap(parseCreationTime)
  }

  private def parseCreationTime(s: String): Option[Long] = {
    if (s == null || s.isEmpty) return None
    val raw = s.trim
    // Try ISO8601 as-is, then with common variants (microseconds, Z suffix, no timezone = UTC)
    Seq(
      raw,
      raw.replaceAll("\\.\\d+Z?$", "Z"),
      if (raw.endsWith("Z")) raw else raw + "Z"
    ).flatMap(s => Try(DateTime.parse(s).getMillis).toOption).headOption
  }
}
