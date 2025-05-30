package controllers

import com.typesafe.config.ConfigFactory
import model.StudentImpl._
import model.{Student, StudentUpdate}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Configuration
import play.api.libs.json.{JsNull, JsString, Json}
import play.api.test.Helpers._
import play.api.test._
import scala.concurrent.ExecutionContext.Implicits.global

class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  val config: Configuration = Configuration(ConfigFactory.load("application.conf"))

  "HomeController" should {

    "render the index page from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents())(config)
      val home       = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    "render the index page from the application" in {
      val controller = inject[HomeController]
      val home       = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    "render the index page from the router" in {
      val request = FakeRequest(GET, "/")
      val home    = route(app, request).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Welcome to Play")
    }

    "return added student" in {
      val auth     = new AuthController(stubControllerComponents())(config)
      val userData = List("username" -> "9Mfl6gyglQ", "password" -> "MyPassword", "grant_type" -> "password")
      val tokenRequest = FakeRequest("POST", "/oauth2/auth")
        .withFormUrlEncodedBody(userData: _*)
      val tokenResponse = auth.accessToken().apply(tokenRequest)
      println(contentAsString(tokenResponse))
      val token = (contentAsJson(tokenResponse) \ "access_token").getOrElse(JsString("")).toString()
      println(token)

      val student     = Student("Vasilyev", "Alexey", "Ignatyevich", "a6", 4.5)
      val studentJson = Json.toJson(student)
      val controller  = inject[HomeController]
      val request = FakeRequest(PUT, "/students/add")
        .withHeaders("Authorization" -> s"Bearer $token")
        .withJsonBody(studentJson)

      val home = route(app, request).get
      println(headers(home))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(student._id.toString)
    }

    "not add student without authorization" in {
      val student     = Student("Vasilyev", "Alexey", "Ignatyevich", "a6", 4.5)
      val studentJson = Json.toJson(student)
      val controller  = inject[HomeController]
      val request = FakeRequest(PUT, "/students/add")
        .withHeaders("Authorization" -> s"Bearer salfjaosifjoasijd")
        .withJsonBody(studentJson)

      val home = route(app, request).get

      status(home) mustBe UNAUTHORIZED
    }

    "return students" in {
      val controller = inject[HomeController]
      val home       = controller.getStudentsList.apply(FakeRequest(GET, "/students/get"))

      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include("Vasilyev")
    }

    "return modified student" in {
      val studentOriginal = Student("Vasilyev", "Alexey", "Ignatyevich", "a6", 4.5)
      val studentUpdate =
        StudentUpdate(surname = Some("Smirnov"), avgScore = Some(4.12), group = Some("c61"), _id = Some(studentOriginal._id))
      val studentJson = Json.toJson(studentUpdate)
      val controller  = inject[HomeController]
      val add         = controller.addStudent().apply(FakeRequest(PUT, "/students/add").withJsonBody(Json.toJson(studentOriginal)))
      val home = add.flatMap { _ =>
        controller
          .updateStudent(studentOriginal._id.toString)
          .apply(FakeRequest(POST, s"/students/update/${studentOriginal._id}")
            .withJsonBody(studentJson))
      }
      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(studentOriginal._id.toString)
    }

    "not return deleted student" in {
      val studentOriginal  = Student("Oderski", "Martin", "", "scala", 5.0)
      val fakeId           = "123456578"
      val alreadyDeletedId = "679b47b874165f71f99cd35c"
      val controller       = inject[HomeController]
      val add              = controller.addStudent().apply(FakeRequest(PUT, "/students/add").withJsonBody(Json.toJson(studentOriginal)))
      val home = add.flatMap(_ =>
        controller
          .deleteStudent(studentOriginal._id.toString)
          .apply(FakeRequest(DELETE, s"/students/delete/${studentOriginal._id}"))
      )
      val home2 = home.flatMap(_ => controller.getStudentsList.apply(FakeRequest(GET, "/students/get")))
      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home2) must not include studentOriginal._id.toString
    }
  }
}
