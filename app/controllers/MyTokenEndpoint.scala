package controllers

import scalaoauth2.provider.{OAuthGrantType, Password, TokenEndpoint}

class MyTokenEndpoint extends TokenEndpoint {
  val passwordNoCred = new Password() {
    override def clientCredentialRequired = false
  }

  override val handlers: Map[String, Password] = Map(
    OAuthGrantType.PASSWORD -> passwordNoCred
  )
}
