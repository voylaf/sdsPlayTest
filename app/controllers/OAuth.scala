package controllers

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{BaseController, ControllerComponents}
import play.mvc.Call
import play.mvc.Http.RequestHeader

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

@Singleton
class OAuth(val controllerComponents: ControllerComponents)(config: Configuration)(ws: WSClient)(implicit ec: ExecutionContext)
    extends BaseController {
  def authorizeUrl(returnTo: Call)(implicit request: RequestHeader): String =
    OAuth.makeUrl(
      config.get[String]("githubAuthEndpoint"),
      "response_type" -> "code",
      "client_id"     -> config.get[String]("githubAppClientId"),
      "redirect_uri"  -> routes.callback().absoluteURL(),
      "state"         -> returnTo.url
    )

  val callback = Action.async { implicit request =>
    request.getQueryString("code") match {
      case Some(code) =>
        val returnTo    = request.getQueryString("state").getOrElse(routes.Items.list().url)
        val callbackUrl = routes.callback().absoluteURL()
        for {
          response <- ws.url(tokenEndpoint).post(Map(
            "code"          -> Seq(code),
            "client_id"     -> Seq(config.get[String]("client")),
            "client_secret" -> Seq(clientSecret),
            "redirect_uri"  -> Seq(callbackUrl),
            "grant_type"    -> Seq("authorization_code")
          ))
        } yield {
          (response.json \ "access_token").validate[String].fold(
            _ => InternalServerError,
            token =>
              Redirect(returnTo)
                .addingToSession(tokenKey -> token)
          )
        }
      case None =>
        Future.successful(InternalServerError)
    }
  }
}

object OAuth {
  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}
    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.mkString("?", "&", "")
  }
}
