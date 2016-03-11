package config

import akka.actor.ActorSystem
import org.scalatest.path
import services.GameActor

/**
  * Created by dnwiebe on 3/8/16.
  */
class GlobalTest extends path.FunSpec {
  describe ("The Global") {
    val system = ActorSystem ()
    val actor = GameActor (100) (system)
    Global.gameActorGetter = {() => actor}

    describe ("before the application starts") {
      describe ("asked what the max score is") {
        val result = Global.maxScore

        it ("knows") {
          assert (result === 100)
        }
      }

      describe ("asked for the game actor") {
        val result = Global.gameActor

        it ("doesn't know") {
          assert (result === null)
        }
      }
    }

    describe ("after the application starts") {
      Global.onStart (null)

      describe ("asked for the game actor") {
        val result = Global.gameActor

        it ("knows") {
          assert (result === actor)
        }
      }
    }
  }
}
