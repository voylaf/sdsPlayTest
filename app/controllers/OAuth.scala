package controllers

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Results}
import play.mvc.Call

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

//@Singleton
//class OAuth @Inject() (val controllerComponents: ControllerComponents)(ws: WSClient)(config: Configuration)(implicit
//    ec: ExecutionContext
//) extends BaseController {
//
//  def authorizeUrl[A](returnTo: Call)(implicit request: Request[A]): String =
//    OAuth.makeUrl(
//      config.get[String]("authEndpoint"),
//      "response_type" -> "code",
//      "client_id"     -> config.get[String]("clientId"),
//      "redirect_uri"  -> routes.OAuth.callback.absoluteURL(),
//      "state"         -> returnTo.url()
//    )
//
//  val callback: Action[AnyContent] = Action.async { implicit request =>
//    request.getQueryString("code") match {
//      case Some(code) =>
////        val returnTo    = request.getQueryString("state").getOrElse(config.get[String]("defaultReturnUrl"))
//        val callbackUrl = routes.OAuth.callback.absoluteURL()
//        val data = Map(
//          "code"          -> Seq(code),
//          "client_id"     -> Seq(config.get[String]("clientId")),
//          "client_secret" -> Seq(config.get[String]("clientSecret")),
//          "redirect_uri"  -> Seq(callbackUrl),
//          "grant_type"    -> Seq("authorization_code")
//        )
//        for {
//          response <- ws.url(config.get[String]("tokenEndpoint")).post(data)
//        } yield {
//          (response.json \ "access_token").validate[String].fold(
//            _ => InternalServerError,
//            token =>
//              Ok.addingToSession("tokenKey" -> token)
//          )
//        }
//      case None =>
//        Future.successful(InternalServerError)
//    }
//  }
//
//  def authenticated[A, B](f: String => A, g: => A)(implicit request: Request[B]): A = {
//    request.session.get("tokenKey") match {
//      case Some(token) => f(token)
//      case None        => g
//    }
//  }
//
//  def authorize(): Action[AnyContent] = Action { implicit request =>
//    authenticated(
//      token => Ok(s"token=$token"),
//      Redirect(authorizeUrl(routes.OAuth.authorize()))
//    )
//  }
//}

object OAuth {
  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}
    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.mkString("?", "&", "")
  }

//  case class Configuration(
//      clientId: String,
//      clientSecret: String,
//      authEndpoint: String,
//      tokenEndpoint: String
//  )
}
