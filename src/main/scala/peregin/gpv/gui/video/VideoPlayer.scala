package peregin.gpv.gui.video


// extract the expected functionality from the player into this interface
// implementation can be easily replaced (media vs tool based from xuggler)
trait VideoPlayer {

  def seek(percentage: Double)

  def close()
}
