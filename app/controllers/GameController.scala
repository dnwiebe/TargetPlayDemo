package controllers

import config.Global
import play.api.libs.json.JsValue
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.Play.current
import services.IncomingActor

/**
  * Created by dnwiebe on 3/6/16.
  */

trait GameController {
  this: Controller =>

  def index = Action {
    Ok (views.html.front_page ())
  }

  def startOrJoin = Action {implicit request =>
    Ok (views.html.game_page (100))
  }

  def socket = WebSocket.acceptWithActor[JsValue, JsValue] {request => out =>
    IncomingActor.props (out, Global.nextId)
  }
}

object GameController extends Controller with GameController
