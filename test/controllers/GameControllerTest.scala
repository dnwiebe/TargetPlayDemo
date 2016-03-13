package controllers

import config.Global
import org.scalatest.path
import play.api.Play
import play.api.mvc._
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._
import services.{AccuracyStats, StatisticsService}
import org.mockito.Mockito._

/**
  * Created by dnwiebe on 3/1/16.
  */
class GameControllerTest extends path.FunSpec {
  val app = FakeApplication (additionalConfiguration = Map (
    "db.default.driver" -> "org.h2.Driver",
    "db.default.url" -> "jdbc:h2:mem:test",
    "db.default.username" -> "sa",
    "db.default.password" -> ""
  ))
  Play.start (app)

  describe ("A GameController with a mocked StatisticsService") {
    val statisticsService = mock (classOf[StatisticsService])
    val oldStatisticsServiceGetter = Global.statisticsServiceGetter
    Global.statisticsServiceGetter = {() => statisticsService}
    Global.onStart (null)
    val subject = new TestGameController ()

    describe ("asked for the result of the index action") {
      when (statisticsService.accuracyStats()).thenReturn (List (
        AccuracyStats (1L, "Tom", 10, 50, 5.0),
        AccuracyStats (2L, "Dick", 5, 25, 5.0)
      ))
      val resultFuture = subject.index.apply(FakeRequest ())

      it ("presents a Start or Join button") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("Start or Join"))
        assert (bodyText.contains ("<td>Tom</td><td>5.0</td>"))
        assert (bodyText.contains ("<td>Dick</td><td>5.0</td>"))
        assert (status (resultFuture) === 200)
      }
    }

    describe ("asked to start or join") {
      describe ("with a name in the form") {
        val request = FakeRequest ("POST", "path").withFormUrlEncodedBody (("name", "Pudge"))

        val resultFuture = subject.startOrJoin (request)

        it ("redirects to the game page with the supplied name") {
          assert (status (resultFuture) === 303)
          assert (redirectLocation (resultFuture) === Some ("/game_page/Pudge"))
        }
      }

      describe ("with no name in the form") {
        when (statisticsService.accuracyStats()).thenReturn (List (
          AccuracyStats (1L, "Tom", 10, 50, 5.0),
          AccuracyStats (2L, "Dick", 5, 25, 5.0)
        ))
        val request = FakeRequest ("POST", "path").withFormUrlEncodedBody (("name", ""))

        val resultFuture = subject.startOrJoin (request)

        it ("presents the front page again, but with an error status and message") {
          assert (status (resultFuture) === 400)
          val bodyText = contentAsString (resultFuture)
          assert (bodyText.contains ("Start or Join"))
          assert (bodyText.contains ("This field is required"))
        }
      }

      describe ("with a script in the form") {
        when (statisticsService.accuracyStats()).thenReturn (List (
          AccuracyStats (1L, "Tom", 10, 50, 5.0),
          AccuracyStats (2L, "Dick", 5, 25, 5.0)
        ))
        val request = FakeRequest ("POST", "path").withFormUrlEncodedBody (("name", "<script>alert('Hi!');</script>"))

        val resultFuture = subject.startOrJoin (request)

        it ("presents the front page again, but with an error status and message") {
          assert (status (resultFuture) === 400)
          val bodyText = contentAsString (resultFuture)
          assert (bodyText.contains ("Start or Join"))
          assert (bodyText.contains ("Script injection?"))
        }
      }
    }

    describe ("asked for the game page with a name") {
      val request = FakeRequest ("GET", "path")

      val resultFuture = subject.gamePage ("Pudge").apply (request)

      it ("presents a page that loads the proper JavaScript") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("GameManager.js"))
        assert (bodyText.contains ("PlayingFieldManager.js"))
        assert (bodyText.contains ("ScoreDisplayManager.js"))
        assert (bodyText.contains ("Utils.js"))
      }

      it ("creates a GameManager with the player's name") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("""GameManager (playingField, scoreDisplay, "Pudge")"""))
      }
    }

    Global.statisticsServiceGetter = oldStatisticsServiceGetter
  }

  class TestGameController extends Controller with GameController
}
