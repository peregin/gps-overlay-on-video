package peregin.gpv.format

object TimeFormatter {

  def formatTime(timeSeconds: Long): String = {
    var sign = math.signum(timeSeconds)
    var remain = math.abs(timeSeconds)
    val seconds = remain % 60
    remain /= 60
    val minutes = remain % 60
    remain /= 60
    val hours = remain

    return String.format(if (sign < 0 && hours == 0) "  -%d:%02d:%02d" else "% 3d:%02d:%02d",  sign * hours, minutes, seconds)
  }
}
