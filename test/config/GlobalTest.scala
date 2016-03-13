package config

import akka.actor.ActorRef
import org.scalatest.path
import services.StatisticsService
import org.mockito.Mockito._

/**
  * Created by dnwiebe on 3/8/16.
  */
class GlobalTest extends path.FunSpec {
  describe ("The Global") {
    val databaseActor = mock (classOf[ActorRef])
    val gameActor = mock (classOf[ActorRef])
    val statisticsService = mock (classOf[StatisticsService])
    Global.databaseActorGetter = {() => databaseActor}
    Global.gameActorGetter = {() => gameActor}
    Global.statisticsServiceGetter = {() => statisticsService}

    describe ("after the application starts") {
      Global.onStart (null)

      describe ("asked for the database actor") {
        val result = Global.databaseActor

        it ("knows") {
          assert (result === databaseActor)
        }
      }

      describe ("asked for the game actor") {
        val result = Global.gameActor

        it ("knows") {
          assert (result === gameActor)
        }
      }

      describe ("asked for the statistics service") {
        val result = Global.statisticsService

        it ("knows") {
          assert (result === statisticsService)
        }
      }

      describe ("asked what the max score is") {
        val result = Global.maxScore

        it ("knows") {
          assert (result === 100)
        }
      }
    }
  }
}
