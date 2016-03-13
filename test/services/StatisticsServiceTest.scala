package services

import java.sql.{Connection, DriverManager}

import anorm._
import org.scalatest.path
import play.api.Play
import play.api.db.Databases
import play.api.test.FakeApplication
import play.db.Database

/**
  * Created by dnwiebe on 3/13/16.
  */
object StatisticsServiceTest {
  private var created = false
  private def ensureTableCreation () = {
    if (!created) {
      DriverManager.registerDriver (new org.h2.Driver ())
      val conn = DriverManager.getConnection ("jdbc:h2:mem:statisticsservicetest")
      StatisticsService._withConnection = { block => block (conn) }
      StatisticsService.withConnection {conn => DatabaseActor.createTables (conn)}
      created = true
    }
  }
}

class StatisticsServiceTest extends path.FunSpec {
  import StatisticsServiceTest._

  val app = FakeApplication (additionalConfiguration = Map (
    "db.default.driver" -> "org.h2.Driver",
    "db.default.url" -> "jdbc:h2:mem:test",
    "db.default.username" -> "sa",
    "db.default.password" -> ""
  ))
  Play.start (app)

  describe ("Given an empty in-memory database") {
    ensureTableCreation()
    implicit val conn = DriverManager.getConnection ("jdbc:h2:mem:statisticsservicetest")
    val subject = new StatisticsService ()

    describe ("and asked for accuracy stats") {
      val accuracyStats = subject.accuracyStats ()

      it ("returns nothing") {
        assert (accuracyStats === Nil)
      }
    }

    describe ("that has a few hits in it") {
      val insertPlayer: (Connection, String) => Option[Long] = {(conn: Connection, name: String) =>
        SQL ("insert into players (name) values ({name})")
          .on ('name -> name)
          .executeInsert ()(conn)
      }
      val insertHit = {(conn: Connection, playerId: Long, points: Int) =>
        SQL ("insert into hits (timestamp, game_id, player_id, points) values (0, 0, {playerId}, {points})")
          .on ('playerId -> playerId, 'points -> points)
          .executeInsert ()(conn)
      }
      var wobbleId = 0L
      var johnnyId = 0L
      var sriramId = 0L
      var toadId = 0L

      StatisticsService.withConnection {implicit conn =>
        wobbleId = insertPlayer (conn, "Wobble").get
        List (1, 1, 1).foreach {points => insertHit (conn, wobbleId, points)}
        johnnyId = insertPlayer (conn, "Johnny").get
        List (1, 2, 3, 4, 5).foreach {points => insertHit (conn, johnnyId, points)}
        sriramId = insertPlayer (conn, "Sriram").get
        List (6, 7, 8, 9, 10).foreach {points => insertHit (conn, sriramId, points)}
        toadId = insertPlayer (conn, "Toad").get
        List (1, 2, 1, 1).foreach {points => insertHit (conn, toadId, points)}
      }

      describe ("and asked for accuracy stats") {
        val accuracyStats = subject.accuracyStats ()

        it ("returns the top three in descending order") {
          assert (accuracyStats === List (
            AccuracyStats (sriramId, "Sriram", 5, 40, 8.0),
            AccuracyStats (johnnyId, "Johnny", 5, 15, 3.0),
            AccuracyStats (toadId, "Toad", 4, 5, 1.25)
          ))
        }
      }
    }
  }
}
