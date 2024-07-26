package peregin.gpv.format

object TimeFormatter {

  def formatTime(timeSeconds: Long): String = {
    var remain = timeSeconds
    val seconds = timeSeconds % 60
    remain /= 60
    val minutes = remain % 60
    remain /= 60
    val hours = remain

    return String.format("% 3d:%02d:%02d", hours, minutes, seconds)
  }
}
