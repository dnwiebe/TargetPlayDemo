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

    describe ("asked for the game page") {
      val resultFuture = subject.startOrJoin(FakeRequest ())

      it ("presents a page that loads the proper JavaScript") {
        val bodyText = contentAsString (resultFuture)
        assert (bodyText.contains ("GameManager.js"))
        assert (bodyText.contains ("PlayingFieldManager.js"))
        assert (bodyText.contains ("ScoreDisplayManager.js"))
        assert (bodyText.contains ("Utils.js"))
      }
    }
  }

  class TestGameController extends Controller with GameController
}
