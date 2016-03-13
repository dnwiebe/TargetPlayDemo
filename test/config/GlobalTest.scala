package config

import akka.actor.ActorSystem
import org.scalatest.path
import services.{DatabaseActor, GameActor}

/**
  * Created by dnwiebe on 3/8/16.
  */
class GlobalTest extends path.FunSpec {
  describe ("The Global") {
    val system = ActorSystem ()
    val databaseActor = DatabaseActor () (system)
    val gameActor = GameActor (databaseActor, 100) (system)
    Global.databaseActorGetter = {() => databaseActor}
    Global.gameActorGetter = {() => gameActor}

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

      describe ("asked what the max score is") {
        val result = Global.maxScore

        it ("knows") {
          assert (result === 100)
        }
      }
    }
  }
}
