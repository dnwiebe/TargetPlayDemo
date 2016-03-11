package controllers

import config.Global
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current
import services.IncomingActor

/**
  * Created by dnwiebe on 3/6/16.
  */

case class UserData (name: String)

trait GameController {
  this: Controller =>

  def index = Action {
    Ok (views.html.front_page ())
  }

  def startOrJoin = Action {implicit request =>
    Form (
      mapping (
        "name" -> text
      )(UserData.apply)(UserData.unapply)
    ).bindFromRequest.fold (handleStartOrJoinFormWithErrors, handleStartOrJoinForm)
  }

  // It's unclear how to unit-test this
  def socket = WebSocket.acceptWithActor[JsValue, JsValue] {request => out =>
    IncomingActor.props (out, Global.gameActor)
  }

  private def handleStartOrJoinForm (userData: UserData) (implicit request: RequestHeader): Result = {
    if (validateUserData (userData)) {
      Ok (views.html.game_page (Global.maxScore, userData.name))
    }
    else {
      Ok (views.html.front_page ())
    }
  }

  private def handleStartOrJoinFormWithErrors (form: Form[UserData]): Result = {
    Ok (views.html.front_page ())
  }

  private def validateUserData (userData: UserData): Boolean = {
    !userData.name.isEmpty
  }
}

object GameController extends Controller with GameController
