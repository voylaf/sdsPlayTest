package controllers

import model.Auth.{MongoAuthOps, UsersAccountingHandler}
import play.api.Configuration
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import scalaoauth2.provider.OAuth2Provider

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthController @Inject() (components: ControllerComponents)(config: Configuration)
    extends AbstractController(components) with OAuth2Provider {
  override val tokenEndpoint = new MyTokenEndpoint()

  val mongoAuthOps = new MongoAuthOps(
    config.get[String]("mongoConnectionString"),
    config.get[String]("mongoDatabase"),
    config.get[String]("mongoUsersCollection")
  )

  val usersAccountingHandler = new UsersAccountingHandler(
    mongoAuthOps,
    config.get[Long]("tokenLifetimeSeconds")
  )

  def accessToken(): Action[AnyContent] = Action.async { implicit request =>
    issueAccessToken(usersAccountingHandler)
  }
}
