package services

import akka.actor.ActorSystem
import org.scalatest.path
import akka.testkit.{TestActorRef, TestKit}
import play.api.libs.json.{JsValue, Json}
import utils.TestUtils.Recorder
import utils.TestUtils.Recorder._

/**
  * Created by dnwiebe on 3/7/16.
  */
class IncomingActorTest (system: ActorSystem) extends TestKit (system) with path.FunSpecLike {

  private implicit val implicitSystem = system

  def this () = {this (ActorSystem ())}

  describe ("An IncomingActor, connected to front end and back end") {
    val frontEnd = Recorder ()
    val backEnd = Recorder ()
    val subject = TestActorRef (new IncomingActor (frontEnd, backEnd))

    describe ("sent a join request from the front end") {
      val jsObject = Json.obj ("type" -> "joinRequest", "data" -> Map ("name" -> "Giggles"))

      subject ! jsObject

      it ("passes the message on to the GameActor") {
        assert (log (backEnd) === List (
          JoinRequest ("Giggles")
        ))
      }
    }

    describe ("sent an invitation with an id, then a score increment from the front end") {
      subject ! Invitation (12, "irrelevant")
      val jsObject = Json.obj ("type" -> "score", "data" -> Map ("increment" -> 7))

      subject ! jsObject

      it ("passes the message on to the GameActor") {
        assert (log (backEnd) === List (
          ScoreIncrement (12, 7)
        ))
      }
    }

    describe ("sent an invitation from the back end") {
      subject ! Invitation (62, "Giggles")

      it ("passes the message on to the front end") {
        val jsValue = log (frontEnd).head.asInstanceOf[JsValue]
        assert ((jsValue \ "type").as[String] === "invitation")
        assert ((jsValue \ "data" \ "id").as[Int] === 62)
        assert ((jsValue \ "data" \ "name").as[String] === "Giggles")
      }
    }

    describe ("sent a set of progress updates from the back end") {
      subject ! List (
        PlayerState (321, None, "Pookie", null, 12),
        PlayerState (432, None, "Chubs", null, 23),
        PlayerState (543, None, "Bullethead", null, 34)
      )

      it ("passes the message on to the front end") {
        val jsValue = log (frontEnd).head.asInstanceOf[JsValue]
        assert ((jsValue \ "type").as[String] === "progress")
        val pookie = (jsValue \ "data")(0)
        assert ((pookie \ "id").as[Int] === 321)
        assert ((pookie \ "name").as[String] === "Pookie")
        assert ((pookie \ "score").as[Int] === 12)
        val chubs = (jsValue \ "data")(1)
        assert ((chubs \ "id").as[Int] === 432)
        assert ((chubs \ "name").as[String] === "Chubs")
        assert ((chubs \ "score").as[Int] === 23)
        val bullethead = (jsValue \ "data")(2)
        assert ((bullethead \ "id").as[Int] === 543)
        assert ((bullethead \ "name").as[String] === "Bullethead")
        assert ((bullethead \ "score").as[Int] === 34)
      }
    }

    describe ("sent a winner message from the back end") {
      subject ! Winner (47, "Useless")

      it ("passes the message on to the front end") {
        val jsValue = log (frontEnd).head.asInstanceOf[JsValue]
        assert ((jsValue \ "type").as[String] === "winner")
        assert ((jsValue \ "data" \ "id").as[Int] === 47)
        assert ((jsValue \ "data" \ "name").as[String] === "Useless")
      }
    }
  }
}
