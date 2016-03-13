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
      subject.tell (JoinRequest ("Freddie"), freddie)

      describe ("followed by another join request with a different name") {
        subject.tell (JoinRequest ("Hamish"), hamish)

        describe ("followed by a score increment") {
          subject.tell (ScoreIncrement (0, 60), freddie)

          describe ("followed by another very big game-ending increment") {
            subject.tell (ScoreIncrement (1, 100), hamish)

            it ("responds with the proper sequence of messages") {
              assert (log (freddie) === List (
                Invitation (0, "Freddie"),
                List (PlayerState (0, "Freddie", freddie, 0)),
                List (PlayerState (0, "Freddie", freddie, 0), PlayerState (1, "Hamish", hamish, 0)),
                List (PlayerState (0, "Freddie", freddie, 60), PlayerState (1, "Hamish", hamish, 0)),
                List (PlayerState (0, "Freddie", freddie, 60), PlayerState (1, "Hamish", hamish, 100)),
                Winner (1, "Hamish")
              ))
              assert (log (hamish) === List (
                Invitation (1, "Hamish"),
                List (PlayerState (0, "Freddie", freddie, 0), PlayerState (1, "Hamish", hamish, 0)),
                List (PlayerState (0, "Freddie", freddie, 60), PlayerState (1, "Hamish", hamish, 0)),
                List (PlayerState (0, "Freddie", freddie, 60), PlayerState (1, "Hamish", hamish, 100)),
                Winner (1, "Hamish")
              ))
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
              List (PlayerState (0, "Freddie", hamish, 5))
            ))
          }
        }
      }
    }
  }
}
