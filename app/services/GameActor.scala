package services

import akka.actor._

/**
  * Created by dnwiebe on 3/6/16.
  */

case class ScoreIncrement (id: Int, increment: Int)
case class JoinRequest (name: String)
case class Invitation (id: Int, name: String)
case class Winner (id: Int, name: String)
case class PlayerState (id: Int, dbId: Option[Long], name: String, representative: ActorRef, score: Int) {
  def afterScoreIncrement (increment: Int): PlayerState = {
    PlayerState (id, dbId, name, representative, score + increment)
  }
  def afterDbIdGrant (dbId: Long): PlayerState = {
    PlayerState (id, Some (dbId), name, representative, score)
  }
}

object GameActor {
  def apply (database: ActorRef, limit: Int) (implicit system: ActorSystem): ActorRef = {
    system.actorOf (Props (classOf[GameActor], database, limit))
  }
}

class GameActor (database: ActorRef, limit: Int) extends Actor {
  private var playerStates: List[PlayerState] = Nil
  private var nextId = 0
  private var gameId: Option[Long] = null

  def receive = {
    case JoinRequest (name) => handleJoinRequest (name)
    case ScoreIncrement (id, increment) => handleScoreIncrement (id, increment)

    case GameId (id) => handleGameId (id)
    case PlayerId (name, id) => handlePlayerId (name, id)
  }

  private def handleJoinRequest (name: String): Unit = {
    if (gameId == null) {
      database ! GameStart (System.currentTimeMillis ())
      gameId = None
    }
    database ! PlayerEntry (name)
    playerStates.find {_.name == name} match {
      case Some (existingPlayer) => {
        val replacement = PlayerState (existingPlayer.id, existingPlayer.dbId, existingPlayer.name, sender, existingPlayer.score)
        existingPlayer.representative ! PoisonPill
        playerStates = playerStates.map {p => if (p.id == existingPlayer.id) replacement else p}
        sender ! Invitation (existingPlayer.id, name)
      }
      case None => {
        playerStates = playerStates ++ List (PlayerState (nextId, None, name, sender, 0))
        sender ! Invitation (nextId, name)
        nextId += 1
      }
    }
    publishScores ()
  }

  private def handleScoreIncrement (id: Int, increment: Int): Unit = {
    playerStates = playerStates.map {playerState =>
      playerState.id == id match {
        case false => playerState
        case true => {
          if (gameId.nonEmpty && playerState.dbId.nonEmpty) {
            database ! PlayerScore (System.currentTimeMillis(), gameId.get, playerState.dbId.get, increment)
          }
          playerState.afterScoreIncrement (increment)
        }
      }
    }
    publishScores ()
  }

  private def handleGameId (id: Long): Unit = {
    gameId = Some (id)
  }

  private def handlePlayerId (name: String, id: Long): Unit = {
    playerStates = playerStates.map {playerState =>
      playerState.name == name match {
        case false => playerState
        case true => playerState.afterDbIdGrant (id)
      }
    }
  }

  private def publishScores (): Unit = {
    val winnerOpt = playerStates.find {_.score >= limit}
    playerStates.foreach {playerState => playerState.representative ! playerStates}
    winnerOpt match {
      case Some (winner) => handleWin (winner)
      case None =>
    }
  }

  private def handleWin (winnerState: PlayerState): Unit = {
    playerStates.foreach {playerState => playerState.representative ! Winner (winnerState.id, winnerState.name)}
    playerStates = Nil
  }

  private case class Entry (id: Int, representative: ActorRef, var score: Int)
}

