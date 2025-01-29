package controllers

import model.{MongoDBActions, StudentActionsMongoDB}
import org.mongodb.scala.MongoDatabase

import javax.inject._
import play.api._
import play.api.mvc._
import play.filters.csrf.CSRF.Token

import scala.concurrent.ExecutionContext.Implicits.global

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)(config: Configuration) extends BaseController {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET` request with a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  /*
    Приложение должно:
    1. Производить авторизацию по протоколу OAuth2.0 и возвращать в ответ
    access_token;
    2. Принимать запросы HTTP GET на получения списка объектов студентов;
    3. Принимать запросы HTTP POST на изменения сущности объекта студента;
    4. Принимать запросы HTTP PUT на добавление новой сущности студента;
    5. Принимать запросы HTTP DELETE на удаление объекта студента.
   */
  private val mongoDatabaseName           = config.get[String]("mongoDatabase")
  lazy val mongoDBActions: MongoDBActions = MongoDBActions.fromConnectionString(config.get[String]("mongoConnectionString"))
  def getStudentsList = Action.async { implicit request: Request[AnyContent] =>
    val studentsFuture = for {
      mongoDatabase <- mongoDBActions.getDatabase(mongoDatabaseName)
      students      <- StudentActionsMongoDB(mongoDb = mongoDatabase, collectionName = config.get[String]("mongoCollection")).getStudentsList
    } yield Ok(students.mkString("\r\n"))
    studentsFuture.recover(e => InternalServerError(e.getStackTrace.mkString("\n")))
  }
}
