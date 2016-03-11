package utils

import akka.actor.{ActorSystem, Actor}
import akka.testkit.TestActorRef

import scala.collection.mutable.ListBuffer

/**
  * Created by dnwiebe on 3/9/16.
  */

object TestUtils {

  object Recorder {
    def apply ()(implicit system: ActorSystem) = TestActorRef (new Recorder ())
    def log (recorder: TestActorRef[Recorder]) = recorder.underlyingActor.log.toList
  }

  class Recorder extends Actor {
    val log = ListBuffer[Any] ()

    def receive = {
      case msg => log.append (msg)
    }
  }
}
