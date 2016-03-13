package services

import java.sql.{Connection, DriverManager}

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestActorRef}
import org.scalatest.path
import anorm._
import anorm.SqlParser._
import utils.TestUtils
import utils.TestUtils.Recorder
import utils.TestUtils.Recorder._

/**
  * Created by dnwiebe on 3/12/16.
  */
object DatabaseActorTest {
  private var created = false
  private def ensureTableCreation () = {
    if (!created) {
      DriverManager.registerDriver (new org.h2.Driver ())
      val conn = DriverManager.getConnection ("jdbc:h2:mem:test")
      DatabaseActor._withConnection = { block => block (conn) }
      DatabaseActor.createTables ()
      created = true
    }
  }
}

class DatabaseActorTest extends TestKit(ActorSystem ()) with path.FunSpecLike {
  import DatabaseActorTest._

  describe ("Given the Play Anorm stuff is subverted to use our in-memory database driver") {
    ensureTableCreation()

    describe ("a DatabaseActor") {
      implicit val conn = DriverManager.getConnection ("jdbc:h2:mem:test")
      val subject = TestActorRef (new DatabaseActor ())
      val recorder = Recorder ()

      describe ("sent a GameStart event") {
        subject.tell (GameStart (1234L), recorder)

        it ("creates a game and replies with its ID") {
          val reportedGameId = log (recorder).last.asInstanceOf[GameId].id
          val (storedGameId, storedTimestamp) = DatabaseActor.withConnection {implicit conn =>
            val query = SQL ("select id, timestamp from games")
            val parser = (long ("id") ~ long ("timestamp")).map {case i ~ t => (i, t)}
            val result = query.as (parser.*)
            result.head
          }
          assert (reportedGameId === storedGameId)
          assert (storedTimestamp === 1234L)
        }
      }

      describe ("sent a PlayerEntry event for a new player") {
        subject.tell (PlayerEntry ("Bobby"), recorder)

        it ("creates a player and replies with its ID") {
          checkForSingleBobby (recorder)
        }

        describe ("and then another PlayerEntry event for the same player name") {
          subject.tell (PlayerEntry ("Bobby"), recorder)

          it ("does not create a player, but returns the id of the existing player") {
            checkForSingleBobby (recorder)
          }
        }
      }

      describe ("sent a PlayerScore event") {
        subject.tell (PlayerScore (1234L, 2345L, 3456L, 7), recorder)

        it ("does not reply, but creates a hits record") {
          assert (log (recorder) === Nil)
          val hits = DatabaseActor.withConnection {implicit conn =>
            val query = SQL ("select timestamp, game_id, player_id, points from hits")
            val parser = (long ("timestamp") ~ long ("game_id") ~ long ("player_id") ~ int ("points")).map {
              case (t ~ gid ~ pid ~ p) => (t, gid, pid, p)
            }
            query.as (parser.*)
          }
          assert (hits === List ((1234L, 2345L, 3456L, 7)))
        }
      }
    }
  }

  private def checkForSingleBobby(recorder: TestActorRef[TestUtils.Recorder])(implicit conn: Connection): Unit = {
    val reportedPlayerId = log (recorder).last.asInstanceOf[PlayerId].id
    val playerPairs = DatabaseActor.withConnection {implicit conn =>
      val query = SQL ("select id, name from players where name = 'Bobby'")
      val parser = (long ("id") ~ str ("name")).map {case i ~ n => (i, n)}
      query.as (parser.*)
    }
    assert (playerPairs === List ((reportedPlayerId, "Bobby")))
  }
}
