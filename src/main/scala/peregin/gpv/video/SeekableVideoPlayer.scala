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
  override def seekBy(deltaMillis: Long) = playerActor ! SeekByCommand(deltaMillis)
  override def close() = video.close()
  override def duration = video.durationInMillis
  override def playing = {
    implicit val timeout = Timeout(5 seconds)
    val future = playerActor ? QueryIsRunning
    Await.result(future, timeout.duration).asInstanceOf[Boolean]
  }

  // FIXME: callback from the actor, subscribe to events instead of callback
  private[video] def handleFrame(frame: FrameIsReady, rotation: Double): Unit = {
    listener.videoEvent(frame.tsInMillis, frame.percentage, frame.image, rotation)
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
  case class SeekByCommand(deltaMillis: Long) extends ControllerCommand // relative seek, resolved against the actor's current position
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
        stay() using frame
      case _ => stay() using EndOfStream
    }

    case Event(SeekCommand(percentage), _) => seekIdle(percentage)

    // resolve the relative offset (in millis) against the CURRENT data of this very message-processing
    // step (guaranteed up to date, since FSM messages are handled strictly one at a time) and act on it
    // immediately, in the same step - never via a self-sent SeekCommand, which would queue behind any
    // other seek requests already sitting in the mailbox (e.g. from fast/held key repeats) and could
    // then be resolved against a position that is no longer current, causing seeks to jump to stale places
    case Event(SeekByCommand(deltaMillis), data) => seekIdle(percentageAfterOffset(deltaMillis, data))

    case Event(PlayCommand, data) =>
      startSingleTimer("nextread", PlayCommand, 500 millis)
      goto(Running) using data
  }

  when(Running) {

    case Event(_, data @ EndOfStream) =>
      log.info("end of the stream has been reached")
      goto(Idle) using data

    case Event(PlayCommand, _) =>
      cancelTimer("nextread") // if something was piled up, remove it from the queue
      video.readNextFrame match {
      case Some(frame @ FrameIsReady(tsInMillis, percentage, keyFrame, _, _)) =>
        handleFrame(frame)
        val delay = video.markDelay(tsInMillis)
        startSingleTimer("nextread", PlayCommand, delay millis)
        stay() using frame
      case _ => goto(Idle) using EndOfStream
    }

    case Event(SeekCommand(percentage), _) => seekRunning(percentage)

    // see the comment on the Idle-state SeekByCommand handler above: resolved and applied synchronously,
    // in this very step, against the data that came with this message
    case Event(SeekByCommand(deltaMillis), data) => seekRunning(percentageAfterOffset(deltaMillis, data))

    case Event(PauseCommand, data) =>
      cancelTimer("nextread")
      goto(Idle) using data
  }

  whenUnhandled {
    case Event(QueryIsRunning, _) =>
      // tells whether the player is still running or not
      sender() ! (stateName == Running)
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

  private def handleFrame(frame: FrameIsReady): Unit = {
    listener.videoEvent(frame.tsInMillis, frame.percentage, frame.image, frame.rotation)
  }

  private def handleSeek(percentage: Double): Unit = {
    listener.seekEvent(percentage)
  }

  private def seekIdle(percentage: Double) = video.seek(percentage) match {
    case Some(seekFrame) =>
      log.info(f"nearest frame found, ts=${TimePrinter.printDuration(seekFrame.tsInMillis)}, @=${seekFrame.percentage}%2.2f")
      handleFrame(seekFrame)
      stay() using seekFrame
    case _ => stay() using EndOfStream
  }

  private def seekRunning(percentage: Double) = {
    cancelTimer("nextread")
    handleSeek(percentage)

    video.seek(percentage) match {
      case Some(seekFrame) =>
        log.info(f"nearest frame found, ts=${TimePrinter.printDuration(seekFrame.tsInMillis)}, @=${seekFrame.percentage}%2.2f")
        video.resetDelay()
        //handleFrame(seekFrame)
        startSingleTimer("nextread", PlayCommand, 0 millis)
        stay() using seekFrame
      case _ => stay() using EndOfStream
    }
  }

  private def currentTsInMillis(data: PacketReply): Long = data match {
    case FrameIsReady(tsInMillis, _, _, _, _) => tsInMillis
    case _ => 0L
  }

  private def percentageAfterOffset(deltaMillis: Long, data: PacketReply): Double = {
    val duration = video.durationInMillis
    if (duration <= 0) 0d
    else {
      val target = math.max(0L, math.min(currentTsInMillis(data) + deltaMillis, duration))
      target * 100.0 / duration
    }
  }
}
