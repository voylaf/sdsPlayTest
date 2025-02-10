package controllers

import model.Auth.MongoAccountingHandler
import play.api.Configuration
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scalaoauth2.provider.OAuth2Provider

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class AuthController @Inject() (components: ControllerComponents)(config: Configuration)
    extends AbstractController(components) with OAuth2Provider {
  override val tokenEndpoint = new MyTokenEndpoint()

  def accessToken(): Action[AnyContent] = Action.async { implicit request =>
    issueAccessToken(
      new MongoAccountingHandler(
        config.get[String]("mongoConnectionString"),
        config.get[String]("mongoDatabase"),
        config.get[String]("mongoUsersCollection")
      )(config.get[Long]("tokenLifetimeSeconds"))
    )
  }
}
