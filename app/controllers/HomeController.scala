package controllers

import model.Auth.{MongoAuthOps, UsersAccountingHandler}
import model.StudentImpl._
import model._
import org.bson.types.ObjectId
import play.api._
import play.api.libs.json._
import play.api.mvc._
import scalaoauth2.provider.OAuth2Provider

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

/** This controller creates an `Action` to handle HTTP requests to the application's home page.
  */
@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)(config: Configuration)
    extends BaseController with OAuth2Provider {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

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
  private val mongoDatabaseName      = config.get[String]("mongoDatabase")
  def mongoDBActions: MongoDBActions = MongoDBActions.fromConnectionString(config.get[String]("mongoConnectionString"))
  def mongoAuthOps = new MongoAuthOps(
    config.get[String]("mongoConnectionString"),
    config.get[String]("mongoDatabase"),
    config.get[String]("mongoUsersCollection")
  )
  val usersAccountingHandler = new UsersAccountingHandler(mongoAuthOps, config.get[Long]("tokenLifetimeSeconds"))
//  2. Принимать запросы HTTP GET на получения списка объектов студентов;
  def getStudentsList: Action[AnyContent] = Action.async { implicit request =>
    val studentsFuture = for {
      mongoDatabase <- mongoDBActions.getDatabase(mongoDatabaseName)
      students <- StudentActionsMongoDB(
        mongoDb = mongoDatabase,
        collectionName = config.get[String]("mongoCollection")
      ).getStudentsList
    } yield Ok("There are students in DB:\n" + students.map(_.show).mkString("\r\n"))
    studentsFuture.recover(e => InternalServerError(e.toString))
  }

//  3. Принимать запросы HTTP POST на изменения сущности объекта студента;
  def addStudent(): Action[AnyContent] = Action.async { implicit request =>
    authorize(usersAccountingHandler) { authInfo =>
      val dbFuture = mongoDBActions.getDatabase(mongoDatabaseName)
      val studentFuture = for {
        student <- Future(Json.fromJson[Student](request.body.asJson.get).get)
        db      <- dbFuture
        _       <- StudentActionsMongoDB(db, config.get[String]("mongoCollection")).addStudent(student)
      } yield Ok(s"$student was added to the DB")

      studentFuture
        .recover {
          case _: java.util.NoSuchElementException =>
            InternalServerError(s"Invalid data - student must have surname, name, patronym, avgScore, and group: ${request.body}")
          case e => InternalServerError(e.toString)
        }
    }
  }

//  4. Принимать запросы HTTP PUT на добавление новой сущности студента;
  def updateStudent(id: String): Action[AnyContent] = Action.async { implicit request =>
    authorize(usersAccountingHandler) { authInfo =>
      val dbFuture = mongoDBActions.getDatabase(mongoDatabaseName)
      val updateFuture = for {
        update  <- Future(Json.fromJson[StudentUpdate](request.body.asJson.get).get)
        db      <- dbFuture
        student <- StudentActionsMongoDB(db, config.get[String]("mongoCollection")).modifyStudentFields(new ObjectId(id), update)
      } yield student
      updateFuture
        .flatMap {
          case Some(student) => Future.successful(Ok(s"Student ($student) was successfully updated"))
          case None          => Future.successful(Ok(s"Student with id=$id wasn't found in the DB"))
        }
        .recover {
          case e => InternalServerError(e.toString)
        }
    }
  }

//  5. Принимать запросы HTTP DELETE на удаление объекта студента.
  def deleteStudent(id: String): Action[AnyContent] = Action.async { implicit request =>
    authorize(usersAccountingHandler) { authInfo =>
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
}
