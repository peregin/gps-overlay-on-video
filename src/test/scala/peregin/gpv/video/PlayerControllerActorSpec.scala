package peregin.gpv.video

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack}
import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AfterExample


class PlayerControllerActorSpec extends TestKit(ActorSystem("test-system"))
  with SpecificationLike with AfterExample with ImplicitSender {

  "controller" should {
    "start reading video stream" in {

      val controller = system.actorOf(Props(new PlayerControllerActor(null, null)))
      controller ! SubscribeTransitionCallBack(testActor)

      expectMsgPF() {
        case CurrentState(_, ReadInProgress) => true
        case _ => false
      }
    }.pendingUntilFixed("in progress")
  }

  override protected def after = system.shutdown()
}
