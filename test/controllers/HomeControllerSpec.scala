package controllers

import com.typesafe.config.ConfigFactory
import model.{Student, StudentUpdate}
import model.StudentImpl._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.Configuration
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration}

/** Add your spec here. You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  val config: Configuration = Configuration(ConfigFactory.load("application.conf"))

  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      val controller = new HomeController(stubControllerComponents())(config)(WsTestClient.withClient(ws => ws))
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
      val student     = Student("Vasilyev", "Alexey", "Ignatyevich", "a6", 4.5)
      val studentJson = Json.toJson(student)
      val controller  = inject[HomeController]
      val home        = controller.addStudent().apply(FakeRequest(PUT, "/students/add").withJsonBody(studentJson))

      println(contentAsString(home))
      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home) must include(student._id.toString)
    }

    "return students" in {
      val controller = inject[HomeController]
      val home       = controller.getStudentsList.apply(FakeRequest(GET, "/students/get"))

      println(contentAsString(home))
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
      println(contentAsString(home))
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
      println(contentAsString(home))
      status(home) mustBe OK
      contentType(home) mustBe Some("text/plain")
      contentAsString(home2) must not include studentOriginal._id.toString
    }
  }
}
