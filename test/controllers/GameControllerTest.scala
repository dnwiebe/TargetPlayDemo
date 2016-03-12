package controllers

import org.scalatest.path
import play.api.Play
import play.api.mvc._
import play.api.test.{FakeApplication, FakeRequest}
import play.api.test.Helpers._

/**
  * Created by dnwiebe on 3/1/16.
  */
class GameControllerTest extends path.FunSpec {
  val app = FakeApplication ()
  Play.start (app)

  describe ("A GameController") {
    val subject = new TestGameController ()

    describe ("asked for the result of the index action") {
      val resultFuture = subject.index.apply(FakeRequest ())

      it ("presents a Start or Join button") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("Start or Join"))
        assert (status (resultFuture) === 200)
      }
    }

    describe ("asked to start or join, with a name in the form") {
      val request = FakeRequest ("POST", "path").withFormUrlEncodedBody(("name", "Pudge"))
      val resultFuture = subject.startOrJoin(request)

      it ("redirects to the game page with the supplied name") {
        assert (status (resultFuture) === 303)
        assert (redirectLocation (resultFuture) === Some ("/game_page/Pudge"))
      }
    }

    describe ("asked to start or join, with no name in the form") {
      val request = FakeRequest ("POST", "path").withFormUrlEncodedBody(("name", ""))
      val resultFuture = subject.startOrJoin(request)

      it ("presents the front page again, but with an error status and message") {
        assert (status (resultFuture) === 400)
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("Start or Join"))
        assert (bodyText.contains ("This field is required"))
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
  }

  class TestGameController extends Controller with GameController
}
