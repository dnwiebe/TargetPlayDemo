package services

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.path
import utils.TestUtils._
import utils.TestUtils.Recorder._

/**
  * Created by dnwiebe on 3/7/16.
  */
class GameActorTest (system: ActorSystem) extends TestKit (system) with path.FunSpecLike {

  private implicit val implicitSystem = system

  def this () = {
    this (ActorSystem ())
  }

  describe ("A GameActor") {
    val database = Recorder ()
    val subject = TestActorRef (new GameActor (database, 100))
    val unidentified = Recorder ()
    val freddie = Recorder ()
    val hamish = Recorder ()

    describe ("that receives a score increment without a join request") {
      subject.tell (ScoreIncrement (2, 60), unidentified)

      it ("ignores it") {
        assert (log (unidentified) === Nil)
      }
    }

    describe ("that receives a join request") {
      val before = System.currentTimeMillis()
      subject.tell (JoinRequest ("Freddie"), freddie)
      val after = System.currentTimeMillis()

      it ("reports both the new game and the join request to the database") {
        val databaseLog = log (database)
        assert (databaseLog.map {x => x.getClass} === List (classOf[GameStart], classOf[PlayerEntry]))
        val gameStart = databaseLog.head.asInstanceOf[GameStart]
        assert (gameStart.timestamp >= before)
        assert (gameStart.timestamp <= after)
        val playerEntry = databaseLog.tail.head.asInstanceOf[PlayerEntry]
        assert (playerEntry === PlayerEntry ("Freddie"))
      }

      describe ("followed by another join request with a different name") {
        subject.tell (JoinRequest ("Hamish"), hamish)

        it ("reports the join request but not a new game to the database") {
          val databaseLog = log (database)
          assert (databaseLog.map {x => x.getClass} === List (classOf[GameStart], classOf[PlayerEntry], classOf[PlayerEntry]))
          val playerEntry = databaseLog(2).asInstanceOf[PlayerEntry]
          assert (playerEntry === PlayerEntry ("Hamish"))
        }

        describe ("followed by a score increment before any results from the database arrive") {
          subject.tell (ScoreIncrement (0, 60), freddie)

          it ("doesn't report the increment to the database") {
            assert (log (database).map {x => x.getClass} === List (classOf[GameStart], classOf[PlayerEntry], classOf[PlayerEntry]))
          }

          describe ("followed by database reports of game and player IDs") {
            subject.tell (GameId (1234L), database)
            subject.tell (PlayerId ("Freddie", 2345L), database)
            subject.tell (PlayerId ("Hamish", 3456L), database)

            describe ("followed by another very big game-ending increment") {
              val before = System.currentTimeMillis()
              subject.tell (ScoreIncrement (1, 100), hamish)
              val after = System.currentTimeMillis ()

              it ("reports the score to the database") {
                val databaseLog = log (database)
                assert (databaseLog.map {x => x.getClass} === List (classOf[GameStart], classOf[PlayerEntry],
                  classOf[PlayerEntry], classOf[PlayerScore]))
                val playerScore = databaseLog(3).asInstanceOf[PlayerScore]
                assert (playerScore.timestamp >= before)
                assert (playerScore.timestamp <= after)
                assert (playerScore.gameId === 1234L)
                assert (playerScore.playerId === 3456L)
                assert (playerScore.points === 100)
              }

              it ("responds with the proper sequence of messages to the players") {
                assert (log (freddie) === List (
                  Invitation (0, "Freddie"),
                  List (PlayerState (0, None, "Freddie", freddie, 0)),
                  List (PlayerState (0, None, "Freddie", freddie, 0), PlayerState (1, None, "Hamish", hamish, 0)),
                  List (PlayerState (0, None, "Freddie", freddie, 60), PlayerState (1, None, "Hamish", hamish, 0)),
                  List (PlayerState (0, Some (2345L), "Freddie", freddie, 60), PlayerState (1, Some (3456L), "Hamish", hamish, 100)),
                  Winner (1, "Hamish")
                ))
                assert (log (hamish) === List (
                  Invitation (1, "Hamish"),
                  List (PlayerState (0, None, "Freddie", freddie, 0), PlayerState (1, None, "Hamish", hamish, 0)),
                  List (PlayerState (0, None, "Freddie", freddie, 60), PlayerState (1, None, "Hamish", hamish, 0)),
                  List (PlayerState (0, Some (2345L), "Freddie", freddie, 60), PlayerState (1, Some (3456L), "Hamish", hamish, 100)),
                  Winner (1, "Hamish")
                ))
              }
            }
          }
        }
      }
    }

    describe ("that receives a join request for Freddie from Freddie's websocket") {
      subject.tell (JoinRequest ("Freddie"), freddie)

      describe ("followed by a score from Freddie") {
        subject.tell (ScoreIncrement (0, 5), freddie)

        describe ("followed by a join request for Freddie from Hamish's websocket") {
          subject.tell (JoinRequest ("Freddie"), hamish)

          it ("replaces Freddie's representative with Hamish's representative, maintaining score and id") {
            assert (log (hamish) === List (
              Invitation (0, "Freddie"),
              List (PlayerState (0, None, "Freddie", hamish, 5))
            ))
          }
        }
      }
    }
  }
}
