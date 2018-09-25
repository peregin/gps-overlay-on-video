package peregin.gpv.video

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import peregin.gpv.video.PlayerControllerActor.{QueryIsRunning, Idle, Running, State}
import peregin.gpv.video.PlayerProtocol._
import peregin.gpv.util.{Logging, TimePrinter}
import scala.language.postfixOps

import scala.concurrent.Await

import scala.concurrent.duration._
import scala.language.postfixOps


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
  override def playing = {
    implicit val timeout = Timeout(5 seconds)
    val future = playerActor ? QueryIsRunning
    Await.result(future, timeout.duration).asInstanceOf[Boolean]
  }

  // FIXME: callback from the actor, subscribe to events instead of callback
  private[video] def handleFrame(frame: FrameIsReady) {
    listener.videoEvent(frame.tsInMillis, frame.percentage, frame.image)
  }

  val system = ActorSystem("gpv")
  val playerActor = system.actorOf(Props(new PlayerControllerActor(video, listener)), name = "playerController")
  // read the first frame
  playerActor ! StepCommand
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
  case object Running extends State
  case object QueryIsRunning extends State
}

class PlayerControllerActor(video: SeekableVideoStream, listener: VideoPlayer.Listener) extends Actor with FSM[State, PacketReply] with LoggingFSM[State, PacketReply] {

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

    case Event(PlayCommand, data) =>
      setTimer("nextread", PlayCommand, 500 millis, repeat = false)
      goto(Running) using data
  }

  when(Running) {

    case Event(_, data @ EndOfStream) =>
      log.info("end of the stream has been reached")
      goto(Idle) using data

    case Event(PlayCommand, _) =>
      cancelTimer("nextread") // if something was piled up, remove it from the queue
      video.readNextFrame match {
      case Some(frame @ FrameIsReady(tsInMillis, percentage, keyFrame, _)) =>
        handleFrame(frame)
        val delay = video.markDelay(tsInMillis)
        setTimer("nextread", PlayCommand, delay millis, repeat = false)
        stay using frame
      case _ => goto(Idle) using EndOfStream
    }

    case Event(SeekCommand(percentage), data) =>
      cancelTimer("nextread")
      handleSeek(percentage)

      video.seek(percentage) match {
        case Some(seekFrame) =>
          log.info(f"nearest frame found, ts=${TimePrinter.printDuration(seekFrame.tsInMillis)}, @=${seekFrame.percentage%2.2f}")
          video.resetDelay()
          //handleFrame(seekFrame)
          setTimer("nextread", PlayCommand, 0 millis, repeat = false)
          stay using seekFrame
        case _ => stay using EndOfStream
    }

    case Event(PauseCommand, data) =>
      cancelTimer("nextread")
      goto(Idle) using data
  }

  whenUnhandled {
    case Event(QueryIsRunning, _) =>
      // tells whether the player is still running or not
      sender ! (stateName == Running)
      stay()
    case any =>
      log.warning(s"unhandled ${any.toString}")
      stay()
  }

  onTransition {
    case Idle -> Running => listener.videoStarted()
    case Running -> Idle => listener.videoStopped()
  }

  startWith(Idle, ReadInProgress)
  initialize()

  private def handleFrame(frame: FrameIsReady) {
    listener.videoEvent(frame.tsInMillis, frame.percentage, frame.image)
  }

  private def handleSeek(percentage: Double) {
    listener.seekEvent(percentage)
  }
}
