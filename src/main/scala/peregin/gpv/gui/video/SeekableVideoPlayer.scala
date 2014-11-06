package peregin.gpv.gui.video

import akka.actor._
import peregin.gpv.gui.video.PlayerControllerActor.{Idle, Run, State}
import peregin.gpv.gui.video.PlayerProtocol._
import peregin.gpv.util.{Logging, TimePrinter}


trait SeekableVideoPlayerFactory extends VideoPlayerFactory {
  override def createPlayer(url: String, listener: VideoPlayer.Listener) =
    new SeekableVideoPlayer(url, listener: VideoPlayer.Listener)
}

class SeekableVideoPlayer(url: String, listener: VideoPlayer.Listener) extends VideoPlayer with Logging {

  val video = new SeekableVideoStream(url)

  override def play() = playerActor ! PlayCommand
  override def step() = playerActor ! StepCommand
  override def pause() = playerActor ! PauseCommand
  override def seek(percentage: Double) = playerActor ! SeekCommand(percentage)
  override def close() = video.close()
  override def duration = video.durationInMillis

  // FIXME: callback from the actor, subscribe to events instead of callback
  private[video] def handleFrame(frame: FrameIsReady) {
    listener.videoEvent(frame.tsInMillis, frame.percentage, frame.image)
  }

  val system = ActorSystem("gpv")
  val playerActor = system.actorOf(Props(new PlayerControllerActor(video, listener)), name = "playerController")
  //playerActor ! PlayCommand
}

object PlayerProtocol {
  sealed trait ControllerCommand
  case object PlayCommand extends ControllerCommand
  case object StepCommand extends ControllerCommand
  case object PauseCommand extends ControllerCommand
  case class SeekCommand(percentage: Double) extends ControllerCommand
}

object PlayerControllerActor {
  sealed trait State
  case object Idle extends State
  case object Run extends State
}

class PlayerControllerActor(video: SeekableVideoStream, listener: VideoPlayer.Listener) extends Actor with FSM[State, PacketReply] {

  when(Idle) {
    case Event(StepCommand, _) => video.readNextFrame match {
      case Some(frame) =>
        log.debug(s"step to ts=${TimePrinter.printDuration(frame.tsInMillis)}")
        handleFrame(frame)
        stay using frame
      case _ => stay using EndOfStream
    }
    case Event(SeekCommand(percentage), _) => video.seek(percentage) match {
      case Some(seekFrame) =>
        log.info(f"nearest frame found, ts=${TimePrinter.printDuration(seekFrame.tsInMillis)}, @=${seekFrame.percentage%2.2f}")
        handleFrame(seekFrame)
        stay using seekFrame
      case _ => stay using EndOfStream
    }
    case Event(PlayCommand, data) => goto(Run) using data
  }

  when(Run) {
    case Event(_, data @ EndOfStream) =>
      log.info("end of the stream has been reached")
      goto(Idle) using data
    case Event(PlayCommand, _) => video.readNextFrame match {
      case Some(frame @ FrameIsReady(tsInMillis, percentage, keyFrame, _)) =>
        log.debug(f"frame received, ts=${TimePrinter.printDuration(tsInMillis)}, @=$percentage%2.2f, keyFrame=$keyFrame")
        handleFrame(frame)

        val delay = video.markDelay(tsInMillis)
        import scala.concurrent.duration._
        if (delay > 0) setTimer("nextread", PlayCommand, delay millis, repeat = false)
        else self -> PlayCommand
        stay using frame
      case _ => goto(Idle) using EndOfStream
    }
    case Event(sc: SeekCommand, data) =>
      cancelTimer("nextread")
      goto(Idle) using data
    case Event(PauseCommand, data) => goto(Idle) using data
  }

  whenUnhandled {
    case any =>
      log.warning(s"unhandled ${any.toString}")
      stay()
  }

  onTransition {
    case Idle -> Run => listener.videoStarted()
    case Run -> Idle => listener.videoStopped()
  }

  startWith(Idle, ReadInProgress)
  initialize()

  private def handleFrame(frame: FrameIsReady) {
    listener.videoEvent(frame.tsInMillis, frame.percentage, frame.image)
  }
}
