package controllers

import model.{MongoDBActions, Student, StudentActionsMongoDB, StudentUpdate}
import org.mongodb.scala.MongoDatabase

import javax.inject._
import play.api._
import play.api.libs.json.JsValue
import play.api.mvc._
import play.filters.csrf.CSRF.Token
import play.api.libs.json._
import model.StudentImpl._
import org.bson.types.ObjectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)(config: Configuration) extends BaseController {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be called when the application receives a `GET` request with a path
    * of `/`.
    */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
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
  def getStudentsList: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val studentsFuture = for {
      mongoDatabase <- mongoDBActions.getDatabase(mongoDatabaseName)
      students <- StudentActionsMongoDB(mongoDb = mongoDatabase, collectionName = config.get[String]("mongoCollection")).getStudentsList
    } yield Ok(students.map(_.show).mkString("\r\n"))
    studentsFuture
      .recover(e => InternalServerError(e.toString))
  }

  def addStudent(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val dbFuture = mongoDBActions.getDatabase(mongoDatabaseName)
    val studentFuture = for {
      student <- Future(Json.fromJson[Student](request.body.asJson.get).get)
      db      <- dbFuture
      _       <- StudentActionsMongoDB(db, config.get[String]("mongoCollection")).addStudent(student)
    } yield student

    studentFuture
      .flatMap(student => Future.successful(Ok(s"$student was added to the DB")))
      .recover {
        case e => InternalServerError(e.toString)
      }
  }

  def updateStudent(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val dbFuture = mongoDBActions.getDatabase(mongoDatabaseName)
    val updateFuture = for {
      update <- Future(Json.fromJson[StudentUpdate](request.body.asJson.get).get)
      db     <- dbFuture
      _      <- StudentActionsMongoDB(db, config.get[String]("mongoCollection")).modifyStudentFields(new ObjectId(id), update)
    } yield update
    updateFuture
      .flatMap(_ => Future.successful(Ok(s"Student with id=$id was successfully updated")))
      .recover {
        case e => InternalServerError(e.toString)
      }
  }

  def deleteStudent(id: String): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val dbFuture = mongoDBActions.getDatabase(mongoDatabaseName)
    val deleteFuture = for {
      db     <- dbFuture
      result <- StudentActionsMongoDB(db, config.get[String]("mongoCollection")).deleteStudent(new ObjectId(id))
    } yield result
    deleteFuture
      .flatMap { message => Future.successful(Ok(message)) }
      .recover {
        case e: java.lang.IllegalArgumentException => UnprocessableEntity("Wrong id: " + e.getMessage)
        case e                                     => InternalServerError(e.toString)
      }
  }
}
