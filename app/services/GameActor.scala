package services

import akka.actor._

/**
  * Created by dnwiebe on 3/6/16.
  */

case class ScoreIncrement (id: Int, increment: Int)
case class JoinRequest (name: String)
case class Invitation (id: Int, name: String)
case class Winner (id: Int, name: String)
case class PlayerState (id: Int, name: String, representative: ActorRef, score: Int) {
  def afterScoreIncrement (increment: Int): PlayerState = {
    PlayerState (id, name, representative, score + increment)
  }
}

object GameActor {
  def apply (limit: Int) (implicit system: ActorSystem): ActorRef = {
    system.actorOf (Props (classOf[GameActor], limit))
  }
}

class GameActor (limit: Int) extends Actor {
  private var playerStates: List[PlayerState] = Nil
  private var nextId = 0

  private var entries: List[Entry] = Nil

  def receive = {
    case msg: JoinRequest => handleJoinRequest (msg.name)
    case increment: ScoreIncrement => handleScoreIncrement (increment.id, increment.increment)
  }

  private def handleJoinRequest (name: String): Unit = {
    playerStates.find {_.name == name} match {
      case Some (existingPlayer) => {
        val replacement = PlayerState (existingPlayer.id, existingPlayer.name, sender, existingPlayer.score)
        existingPlayer.representative ! PoisonPill
        playerStates = playerStates.map {p => if (p.id == existingPlayer.id) replacement else p}
        sender ! Invitation (existingPlayer.id, name)
      }
      case None => {
        playerStates = playerStates ++ List (PlayerState (nextId, name, sender, 0))
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
        case true => playerState.afterScoreIncrement (increment)
      }
    }
    publishScores ()
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

