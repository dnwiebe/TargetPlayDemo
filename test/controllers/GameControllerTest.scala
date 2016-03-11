package controllers

import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

/**
  * Created by dnwiebe on 3/1/16.
  */
class GameControllerTest extends org.scalatest.path.FunSpec {
  describe ("A GameController") {
    val subject = new TestGameController ()

    describe ("asked for the result of the index action") {
      val resultFuture = subject.index.apply(FakeRequest ())

      it ("presents a Start or Join button") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("Start or Join"))
      }
    }

    describe ("asked for the game page, with a name in the form") {
      val request = FakeRequest ("POST", "path").withFormUrlEncodedBody(("name", "Pudge"))
      val resultFuture = subject.startOrJoin(request)

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

    describe ("asked for the game page, with no name in the form") {
      val request = FakeRequest ("POST", "path").withFormUrlEncodedBody(("name", ""))
      val resultFuture = subject.startOrJoin(request)

      it ("presents the front page again") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("Start or Join"))
      }
    }
  }

  class TestGameController extends Controller with GameController
}
