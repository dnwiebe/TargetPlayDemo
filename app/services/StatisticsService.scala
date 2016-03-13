package services

import java.sql.Connection
import play.api.Play.current
import play.api.db.DB
import anorm._
import anorm.SqlParser._

/**
  * Created by dnwiebe on 3/13/16.
  */

case class AccuracyStats (id: Long, name: String, count: Int, total: Int, average: Double)

object StatisticsService {
  def withConnection[A] (block: Connection => A): A = {_withConnection (block).asInstanceOf[A]}
  var _withConnection: (Connection => Any) => Any = {block => DB.withConnection[Any] (block)}
}

class StatisticsService () {
  import StatisticsService._

  def accuracyStats (): List[AccuracyStats] = {
    withConnection {implicit conn =>
      val query = SQL (
        """
          | select
          |   players.id as player_id,
          |   players.name as player_name,
          |   count (*) as count,
          |   sum (hits.points) as total,
          |   avg (cast (hits.points as float)) as average
          | from
          |   players
          |   left join hits on
          |     (players.id = hits.player_id)
          | group by
          |   players.id,
          |   players.name
          | order by
          |   average desc
        """.stripMargin)
      val parser = (
        long ("player_id")
        ~ str ("player_name")
        ~ int ("count")
        ~ int ("total")
        ~ double ("average")
      ).map {case i ~ n ~ c ~ t ~ a => AccuracyStats (i, n, c, t, a)}
      query.as (parser.*).take (3)
    }
  }
}
