package controllers

import com.typesafe.config.ConfigFactory
import model.Auth.User.hashString
import model.Auth.User
import org.mongodb.scala.bson.ObjectId
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.test.Helpers.{contentAsString, contentType, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import org.scalatest.BeforeAndAfterAll
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.parsing.json.JSONObject

class AuthControllerSpec extends PlaySpec with BeforeAndAfterAll with GuiceOneAppPerTest with Injecting {
  val config: Configuration = Configuration(ConfigFactory.load("application.conf"))
  val newId                 = new ObjectId()
  val password              = "MyPassword"
  val user: User            = User(newId, "Test", hashString(password))
  val controller            = new AuthController(stubControllerComponents())(config)

  override def beforeAll(): Unit = {
    Await.result(controller.usersAccountingHandler.addUser(user), Duration.Inf)
  }

//  override def afterAll(): Unit = {
//    Await.result(controller.mongoAuthOps.getUsersCollection.flatMap(_.drop().toFuture()), Duration.Inf)
//  }

  "AuthController" should {

    "create access token and return it" in {
      val data     = List(("username", user.name), ("password", password), ("grant_type", "password"))
      val request  = FakeRequest(POST, "/oauth2/auth").withFormUrlEncodedBody(data: _*)
      val response = controller.accessToken()(request)

      println(contentAsString(response))
      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      val token = contentAsJson(response)
      assert((token \ "access_token").toOption.nonEmpty)
      assert((token \ "expires_in").toOption.nonEmpty)
      assert((token \ "refresh_token").toOption.nonEmpty)
      assert((token \ "token_type").toOption.contains(JsString("Bearer")))
    }
  }
}
