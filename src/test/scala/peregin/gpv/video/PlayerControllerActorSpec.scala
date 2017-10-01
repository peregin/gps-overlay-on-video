package peregin.gpv.video

import java.util.concurrent.TimeUnit

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.{AfterEach, AfterSpec}
import peregin.gpv.util.Logging
import peregin.gpv.video.PlayerControllerActor.Idle

import scala.concurrent.duration.Duration


class PlayerControllerActorSpec extends TestKit(ActorSystem("test-system"))
  with SpecificationLike with AfterEach with ImplicitSender with Mockito with Logging {

  val timeout = Duration(1, TimeUnit.SECONDS)

  "controller" should {

    "start reading video stream" in {
      val videoStream = mock[SeekableVideoStream]
      val controller = system.actorOf(Props(new PlayerControllerActor(videoStream, null)))
      controller ! SubscribeTransitionCallBack(testActor)

      expectMsg(timeout, CurrentState(controller, Idle))

      ok("succeeded")
    }//.pendingUntilFixed("in progress")
  }

  override protected def after = system.terminate()
}
