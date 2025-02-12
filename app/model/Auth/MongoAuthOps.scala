package model.Auth

import model.MongoDBActions
import org.mongodb.scala.model.{Filters, Updates}
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import play.api.Logging
import scalaoauth2.provider.{AccessToken, AuthInfo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/*
MongoAuthOps — это класс для работы с аутентификацией пользователей в MongoDB.
Он предоставляет различные операции для добавления пользователей, поиска их по имени и паролю, а также для работы с токенами доступа (Access Token).
Класс использует MongoDB и предоставляет интерфейс для выполнения операций с коллекцией пользователей.
@param connectionString: строка подключения к MongoDB.
@param dbName: имя базы данных MongoDB.
@param usersCollection: имя коллекции в базе данных, содержащей пользователей.
 */
@Singleton
class MongoAuthOps @Inject() (
    connectionString: String,
    dbName: String,
    usersCollection: String
)(implicit ec: ExecutionContext) extends OAuthOps {
  /*
  @return клиент MongoDB, который используется для подключения к базе данных с использованием строки подключения.
   */
  def mongoClient: MongoClient = MongoDBActions.clientFromConnectionString(connectionString)

  /*
  @return Future с объектом базы данных MongoDB, который представляет собой подключение к указанной базе данных (dbName).
   */
  def getDatabase: Future[MongoDatabase] = MongoDBActions(mongoClient).getDatabase(dbName)

  /*
   @return Future с коллекцией пользователей MongoCollection[User], извлекая её из базы данных.
   */
  def getUsersCollection: Future[MongoCollection[User]] = getDatabase.map(md => md.getCollection(usersCollection))

  /*
  Добавляет нового пользователя в коллекцию пользователей.
  Выполняет вставку документа пользователя в коллекцию.
   */
  def addUser(user: User): Future[Unit] = for {
    users <- getUsersCollection
    _     <- users.insertOne(user).toFuture()
  } yield ()

  /*
  Ищет пользователя в БД по имени и паролю. Пароль хэшируется перед поиском.
   */
  def findUserByNameAndPassword(name: String, password: String): Future[Option[User]] = {
    for {
      users <- getUsersCollection
      user <- users.find(
        Filters.and(
          Filters.eq("name", name),
          Filters.eq("hashedPassword", User.hashString(password))
        )
      ).first().toFutureOption()
    } yield user
  }

  /*
  Ищет пользователя в БД по токену доступа. Токен хранится в поле accessToken.token.
   */
  def findUserByAccessToken(token: String): Future[Option[User]] = {
    for {
      users     <- getUsersCollection
      maybeUser <- users.find(Filters.eq("accessToken.token", token)).first().toFutureOption()
    } yield maybeUser
  }

  /*
  Получает сохранённый токен доступа для пользователя на основе переданной информации о пользователе (authInfo).
   */
  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = {
    for {
      users       <- getUsersCollection
      maybeUser   <- users.find(Filters.eq("_id", authInfo.user._id)).first().toFutureOption()
      user        <- Future(maybeUser.get)
      accessToken <- Future(user.accessToken)
    } yield accessToken
  }

  /*
  Сохраняет токен доступа для пользователя в коллекции пользователей.
   */
  def saveAccessToken(user: User, accessToken: AccessToken): Future[Unit] = for {
    users <- getUsersCollection
    res   <- users.updateOne(Filters.equal("_id", user._id), Updates.set("accessToken", accessToken)).toFutureOption()
  } yield ()

  /*
  Удаляет токен доступа пользователя из коллекции.
   */
  def deleteAccessToken(user: User): Future[Unit] =
    getUsersCollection.flatMap(users =>
      users.updateOne(
        Filters.equal("_id", user._id),
        Updates.unset("accessToken")
      ).toFutureOption()
    ).map(_ => ())

  /*
  Удаляет все документы из коллекции пользователей.
   */
  def deleteUsersCollection(): Future[Unit] =
    getUsersCollection.flatMap { _.deleteMany(Filters.notEqual("name", "")).toFuture().map(_ => ()) }

}
