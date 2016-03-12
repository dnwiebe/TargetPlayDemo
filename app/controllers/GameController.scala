package controllers

import config.Global
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.{Valid, Invalid, Constraint}
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import services.IncomingActor

/**
  * Created by dnwiebe on 3/6/16.
  */

trait GameController {
  this: Controller =>

  import GameController._

  def index = Action {
    Ok (views.html.front_page (makeUserDataForm))
  }

  def startOrJoin = Action {implicit request =>
    makeUserDataForm.bindFromRequest.fold (
      {form => BadRequest (views.html.front_page (form))},
      {userData => Redirect (routes.GameController.gamePage (userData.name))}
    )
  }

  def gamePage (name: String) = Action {implicit request =>
    Ok (views.html.game_page (Global.maxScore, name))
  }

  // It's unclear how to unit-test this
  def socket = WebSocket.acceptWithActor[JsValue, JsValue] {request => out =>
    IncomingActor.props (out, Global.gameActor)
  }

  private def makeUserDataForm: Form[UserData] = {
    Form (
      mapping (
        "name" -> nonEmptyText.verifying (scriptInjection)
      )(UserData.apply)(UserData.unapply)
    )
  }

  private val scriptInjection: Constraint[String] = Constraint (None, Nil)({
    case s: String if s.contains ("<script>") =>
      Invalid ("Seriously? Script injection? From you? I thought we were past that.")
    case _ => Valid
  })
}

object GameController extends Controller with GameController {
  case class UserData (name: String)
}
