package services

import akka.actor.{ActorSystem, Props, ActorRef, Actor}

/**
  * Created by dnwiebe on 3/6/16.
  */

case class ScoreIncrement (id: Int, increment: Int)

object GameActor {
  def apply (implicit system: ActorSystem): ActorRef = {
    system.actorOf (Props (classOf[GameActor]))
  }
}

class GameActor extends Actor {
  private val entries: scala.collection.mutable.Map[Int, Entry] = scala.collection.mutable.Map ()

  def receive = {
    case increment: ScoreIncrement => handleScoreIncrement (increment.id, increment.increment)
  }

  private def handleScoreIncrement (id: Int, increment: Int): Unit = {
    if (entries.contains (id)) {
      entries(id).score += increment
    }
    else {
      entries(id) = Entry (id, sender (), increment)
    }
    publishScores ()
  }

  private def publishScores (): Unit = {
    val gameOver = entries.values.foldLeft (false) {(soFar, entry) =>
      val scores = entry.score :: entries.values.filter {v => v.id != entry.id}.map {e => e.score}.toList
      entry.representative ! scores
      soFar || entry.score >= 100
    }
    if (gameOver) {
      entries.values.foreach {entry =>
  println (s"Stopping id ${entry.id}")
        entry.representative ! Stop ()
      }
      entries.clear ()
    }
  }

  private case class Entry (id: Int, representative: ActorRef, var score: Int)
}

