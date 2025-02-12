package model.Auth

import scalaoauth2.provider._
import java.security.SecureRandom
import java.util.Date
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class UsersAccountingHandler(val OAuthOps: OAuthOps, tokenLifeSeconds: Long)(implicit ec: ExecutionContext) extends DataHandler[User] {

  def validateClient(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = {
    Future.successful(true)
  }

  def addUser(user: User): Future[Unit] = OAuthOps.addUser(user)

  def saveAccessToken(user: User, accessToken: AccessToken): Future[Unit] =
    OAuthOps.saveAccessToken(user, accessToken)

  def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] = request match {
    case r: PasswordRequest => OAuthOps.findUserByNameAndPassword(r.username, r.password)
    case _                  => Future.successful(None)
  }

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
    def randomString(length: Int) = new Random(new SecureRandom()).alphanumeric.take(length).mkString
    val createdAt                 = Date.from(java.time.Instant.now())
    val accessToken = AccessToken(
      token = randomString(40),
      refreshToken = Some(randomString(40)),
      scope = None,
      lifeSeconds = Some(tokenLifeSeconds),
      createdAt = createdAt
    )
    saveAccessToken(authInfo.user, accessToken).flatMap(_ => Future.successful(accessToken))
  }

  def deleteAccessToken(user: User): Future[Unit] =
    OAuthOps.deleteAccessToken(user)

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] =
    OAuthOps.getStoredAccessToken(authInfo)

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = for {
    _           <- OAuthOps.deleteAccessToken(authInfo.user)
    accessToken <- createAccessToken(authInfo)
    _           <- OAuthOps.saveAccessToken(authInfo.user, accessToken)
  } yield accessToken

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = {
    Future.successful(throw new NotImplementedError())
  }

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] =
    Future.successful(throw new NotImplementedError())

  def deleteAuthCode(code: String): Future[Unit] =
    Future.successful(throw new NotImplementedError())

  def findAccessToken(token: String): Future[Option[AccessToken]] =
    findUserByToken(token).map(maybeUser => maybeUser.flatMap(_.accessToken))

  def findUserByToken(token: String): Future[Option[User]] =
    OAuthOps.findUserByAccessToken(token)

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] =
    findUserByToken(accessToken.token).map { opt =>
      opt.map { user =>
        AuthInfo[User](
          user = user,
          clientId = None,
          scope = None,
          redirectUri = None
        )
      }
    }
}
