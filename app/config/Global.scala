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
  def gameActor: ActorRef = _gameActor
  def nextId: Int = {
    val retval = _nextId
    _nextId += 1
    retval
  }

  private var _nextId = 0
  private var _gameActor: ActorRef = null

  override def onStart (app: Application): Unit = {
    _gameActor = GameActor (Akka.system)
  }
}
