package config

import akka.actor.ActorRef
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}
import services.GameActor

/**
  * Created by dnwiebe on 3/6/16.
  */

object Global extends GlobalSettings {
  var gameActorGetter = {() => GameActor (maxScore) (Akka.system)}

  def maxScore = 100
  def gameActor: ActorRef = _gameActor

  private var _gameActor: ActorRef = null

  override def onStart (app: Application): Unit = {
    _gameActor = gameActorGetter ()
  }
}
