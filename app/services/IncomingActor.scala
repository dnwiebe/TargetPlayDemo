package services

import akka.actor.{PoisonPill, Actor, ActorRef, Props}
import config.Global
import play.api.libs.json.{JsString, JsArray, JsNumber, JsObject}

/**
  * Created by dnwiebe on 3/6/16.
  */

case class Stop ()

object IncomingActor {
  def props (out: ActorRef, id: Int) = Props (classOf[IncomingActor], out, id)
}

class IncomingActor (out: ActorRef, id: Int) extends Actor {

  def receive = {
    case jsObject: JsObject => handleJsValue (jsObject)

    case scores: List[_] => handleScores (scores.asInstanceOf[List[Int]])
    case msg: Stop => handleStop ()
  }

  private def handleJsValue (jsObject: JsObject) {
    (jsObject \ "type").as[String] match {
      case opcode if opcode == "score" => handleScoreIncrement ((jsObject \"data" \ "increment").as[Int])
    }
  }

  private def handleScoreIncrement (increment: Int): Unit = {
    Global.gameActor ! ScoreIncrement (id, increment)
  }

  private def handleScores (scores: List[Int]): Unit = {
    val jsScores = scores.map {s => JsNumber (s)}
    val jsScoreArray = JsArray (jsScores)
    val jsObject = JsObject (Seq (("type", JsString ("progress")), ("data", jsScoreArray)))
    out ! jsObject
  }

  private def handleStop (): Unit = {
    val jsObject = JsObject (Seq (("type", JsString ("stop")), ("data", JsObject (Nil))))
    out ! jsObject
    self ! PoisonPill
  }
}
