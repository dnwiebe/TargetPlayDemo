package services

import java.sql.Connection
import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import akka.actor.{Props, Actor, ActorSystem, ActorRef}

/**
  * Created by dnwiebe on 3/12/16.
  */

case class GameStart (timestamp: Long)
case class PlayerEntry (name: String)
case class PlayerScore (timestamp: Long, gameId: Long, playerId: Long, points: Int)

case class GameId (id: Long)
case class PlayerId (name: String, id: Long)

object DatabaseActor {
  def apply ()(implicit system: ActorSystem): ActorRef = {
    system.actorOf (Props (classOf[DatabaseActor]))
  }

  def withConnection[A] (block: Connection => A): A = {_withConnection (block).asInstanceOf[A]}
  var _withConnection: (Connection => Any) => Any = {block => DB.withConnection[Any] (block)}

  def createTables (implicit conn: Connection): Unit = {
    ddl ("""
       | create table games (
       |   id bigint auto_increment primary key,
       |   timestamp bigint not null,
       |   winner bigint null
       | )
     """.stripMargin)
    ddl ("""
       | create table players (
       |   id bigint auto_increment primary key,
       |   name varchar not null
       | )
     """.stripMargin)
    ddl ("""
       | create table hits (
       |   id bigint auto_increment primary key,
       |   timestamp bigint not null,
       |   game_id bigint not null,
       |   player_id bigint not null,
       |   points int not null
       | )
     """.stripMargin)
  }

  private def ddl (sql: String)(implicit conn: Connection): Unit = {
    val stmt = conn.createStatement ()
    stmt.execute (sql)
    stmt.close ()
  }
}

class DatabaseActor extends Actor {
  import DatabaseActor._

  def receive = {
    case GameStart (timestamp) => handleGameStart (timestamp)
    case PlayerEntry (name) => handlePlayerEntry (name)
    case PlayerScore (timestamp, gameId, playerId, points) => handlePlayerScore (timestamp, gameId, playerId, points)
  }

  private def handleGameStart (timestamp: Long): Unit = {
    val id = insertGame (timestamp)
    sender ! GameId (id)
  }

  private def handlePlayerEntry (name: String): Unit = {
    val id = insertPlayer (name)
    sender ! PlayerId (name, id)
  }

  private def handlePlayerScore (timestamp: Long, gameId: Long, playerId: Long, points: Int): Unit = {
    withConnection {implicit conn =>
      SQL ("insert into hits (timestamp, game_id, player_id, points) values ({timestamp}, {gameId}, {playerId}, {points})")
        .on ('timestamp -> timestamp, 'gameId -> gameId, 'playerId -> playerId, 'points -> points)
        .executeInsert ()
    }
  }

  private def insertGame (timestamp: Long): Long = {
    val idOpt: Option[Long] = withConnection {implicit conn =>
      SQL ("insert into games (timestamp) values ({timestamp})")
        .on ('timestamp -> timestamp)
        .executeInsert ()
    }
    if (idOpt.isEmpty) {throw new IllegalStateException ("Internal error: table is malformed")}
    idOpt.get
  }

  private def insertPlayer (name: String): Long = {
    val idOpt: Option[Long] = withConnection {implicit conn =>
      val query = SQL ("select id, name from players where name = {name}").on ('name -> name)
      val parser = (long ("id") ~ str ("name")).map {case i ~ n => (i, n)}
      val existingPlayersWithName = query.as (parser.*)
      if (existingPlayersWithName.nonEmpty) {
        if (existingPlayersWithName.length > 1) {throw new IllegalStateException (s"Internal error: multiple players named ${name}")}
        Some (existingPlayersWithName.head._1)
      }
      else {
        SQL ("insert into players (name) values ({name})")
          .on ('name -> name)
          .executeInsert ()
      }
    }
    if (idOpt.isEmpty) {throw new IllegalStateException ("Internal error: table is malformed")}
    idOpt.get
  }
}
