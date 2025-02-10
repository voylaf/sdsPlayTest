package model.Auth

import scalaoauth2.provider.{AccessToken, AuthInfo}

import scala.concurrent.Future

trait OAuthOps {
  def addUser(user: User): Future[Unit]
  def findUserByNameAndPassword(name: String, password: String): Future[Option[User]]
  def findUserByAccessToken(token: String): Future[Option[User]]
  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]]
  def saveAccessToken(user: User, accessToken: AccessToken): Future[Unit]
  def deleteAccessToken(user: User): Future[Unit]
}

object OAuth {
  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}
    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.mkString("?", "&", "")
  }

}
