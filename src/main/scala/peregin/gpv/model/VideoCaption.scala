package peregin.gpv.model

import com.fasterxml.jackson.annotation.JsonFormat

import java.time.Instant


/**
 * Video caption (subtitle).  Not yet supported in GUI, only via project_file.captions:
 * <code>
 *   captions: [
 *        {
 *            "start" : "2024-09-07T18:16:55Z",
 *            "duration" : 15.0,
 *            "text" : "And my sprint to finish and few photos"
 *        }
 *   ]
 * </code>
 * @param start
 * @param duration
 * @param text
 */
case class VideoCaption(
                         @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
                         var start: Option[Instant],
                         var videoTime: Option[Double],
                         var duration: Double,
                         var text: String
                       ) {

  override def toString: String = f"${getClass.getSimpleName}($start,$videoTime,$duration%.01f,$text)"
}
