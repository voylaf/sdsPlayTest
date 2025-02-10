package model.Auth

import model.Id
import scalaoauth2.provider.AccessToken

import java.security.MessageDigest

final case class User(_id: Id, name: String, hashedPassword: String, accessToken: Option[AccessToken] = None)

object User {
  def hashString(string: String): String = {
    MessageDigest.getInstance("SHA-256")
      .digest(string.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }
}

object UserImpl {
  import play.api.libs.json._
  import model.StudentImpl._
  implicit val tokenFormat: Format[AccessToken] = Json.using[Json.WithDefaultValues].format[AccessToken]
  implicit val userFormat: Format[User]         = Json.using[Json.WithDefaultValues].format[User]
}
