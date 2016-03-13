package config

import akka.actor.ActorRef
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}
import services.{DatabaseActor, GameActor}

/**
  * Created by dnwiebe on 3/6/16.
  */

object Global extends GlobalSettings {
  var databaseActorGetter = {() => DatabaseActor () (Akka.system)}
  var gameActorGetter = {() => GameActor (_databaseActor, maxScore) (Akka.system)}

  def maxScore = 100
  def databaseActor: ActorRef = _databaseActor
  def gameActor: ActorRef = _gameActor

  private var _databaseActor: ActorRef = null
  private var _gameActor: ActorRef = null

  override def onStart (app: Application): Unit = {
    _databaseActor = databaseActorGetter ()
    _gameActor = gameActorGetter ()
  }
}
