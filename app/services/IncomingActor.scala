package services

import akka.actor.{PoisonPill, Actor, ActorRef, Props}
import play.api.libs.json._

/**
  * Created by dnwiebe on 3/6/16.
  */

object IncomingActor {
  def props (out: ActorRef, gameActor: ActorRef) = Props (classOf[IncomingActor], out, gameActor)
}

class IncomingActor (out: ActorRef, gameActor: ActorRef) extends Actor {
  var id: Option[Int] = None

  def receive = {
    // from front end
    case jsObject: JsObject => handleJsValue (jsObject)

    // from back end
    case invitation: Invitation => handleInvitation (invitation.id, invitation.name)
    case progress: List[_] => handleProgress (progress.asInstanceOf[List[PlayerState]])
    case winner: Winner => handleWinner (winner.id, winner.name)
  }

  private def handleJsValue (jsObject: JsObject) {
    (jsObject \ "type").as[String] match {
      case opcode if opcode == "joinRequest" => handleJoinRequest ((jsObject \ "data" \ "name").as[String])
      case opcode if opcode == "score" => handleScoreIncrement ((jsObject \ "data" \ "increment").as[Int])
    }
  }

  private def handleJoinRequest (name: String): Unit = {
    gameActor ! JoinRequest (name)
  }

  private def handleInvitation (id: Int, name: String): Unit = {
    this.id = Some (id)
    val jsObject = Json.obj ("type" -> "invitation", "data" -> Map ("id" -> JsNumber (id), "name" -> JsString (name)))
    out ! jsObject
  }

  private def handleProgress (progress: List[PlayerState]): Unit = {
    val jsStates = progress.map {p =>
      Json.obj ("id" -> p.id, "name" -> p.name, "score" -> p.score)
    }
    val jsObject = Json.obj ("type" -> "progress", "data" -> jsStates)
    out ! jsObject
  }

  private def handleScoreIncrement (increment: Int): Unit = {
    // TODO: don't crash here if ID hasn't been set yet
    gameActor ! ScoreIncrement (id.get, increment)
  }

  private def handleWinner (id: Int, name: String): Unit = {
    val jsObject = Json.obj ("type" -> "winner", "data" -> Map ("id" -> JsNumber (id), "name" -> JsString (name)))
    out ! jsObject
    self ! PoisonPill
  }
}
