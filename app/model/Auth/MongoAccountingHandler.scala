package model.Auth

import model.{Id, MongoDBActions}
import org.mongodb.scala.model.{Filters, Updates}
import org.mongodb.scala.result.UpdateResult
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import scalaoauth2.provider._

import java.security.SecureRandom
import java.util.Date
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class MongoAccountingHandler(connectionString: String, dbName: String, usersCollection: String)(tokenLifeSeconds: Long)(implicit
    ec: ExecutionContext
) extends DataHandler[User] {

  val mongoClient: MongoClient = MongoDBActions.clientFromConnectionString(connectionString)

  def getDatabase: Future[MongoDatabase] = MongoDBActions(mongoClient).getDatabase(dbName)

  def getUsersCollection: Future[MongoCollection[User]] = getDatabase.map(md => md.getCollection(usersCollection))

  def validateClient(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = {
    Future.successful(true)
  }

  def addUser(user: User): Future[Unit] = for {
    users <- getUsersCollection
    _     <- users.insertOne(user).toFuture()
  } yield ()

  def saveAccessToken(user: User, accessToken: AccessToken): Future[Option[UpdateResult]] = for {
    users <- getUsersCollection
    res   <- users.updateOne(Filters.equal("_id", user._id), Updates.set("accessToken", accessToken)).toFutureOption()
  } yield res

  def findUser(maybeClientCredential: Option[ClientCredential], request: AuthorizationRequest): Future[Option[User]] = request match {
    case r: PasswordRequest => findUserByNameAndPassword(r.username, r.password)
    case _                  => Future.successful(None)
  }

  def findUserByNameAndPassword(name: String, password: String): Future[Option[User]] =
    for {
      users <- getUsersCollection
      user <- users.find(
        Filters.and(
          Filters.equal("name", name),
          Filters.equal("hashedPassword", User.hashString(password))
        )
      ).first().toFutureOption()
    } yield user

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
    def randomString(length: Int) = new Random(new SecureRandom()).alphanumeric.take(length).mkString
    val createdAt                 = Date.from(java.time.Instant.now())
    Future.successful(
      AccessToken(
        token = randomString(40),
        refreshToken = Some(randomString(40)),
        scope = None,
        lifeSeconds = Some(tokenLifeSeconds),
        createdAt = createdAt
      )
    )
  }

  def deleteAccessToken(user: User): Future[Option[UpdateResult]] = {
    getUsersCollection.flatMap(users =>
      users.updateOne(
        Filters.equal("_id", user._id),
        Updates.unset("accessToken")
      ).toFutureOption()
    )
  }

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] =
    getUsersCollection.flatMap(users =>
      users.find(Filters.equal("_id", authInfo.user._id)).first().toFutureOption()
        .map(opt => opt.flatMap(_.accessToken))
    )

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = for {
    _           <- deleteAccessToken(authInfo.user)
    accessToken <- createAccessToken(authInfo)
  } yield accessToken

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = {
    Future.successful(throw new NotImplementedError())
  }

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] =
    Future.successful(throw new NotImplementedError())

  def deleteAuthCode(code: String): Future[Unit] =
    Future(throw new NotImplementedError())

  def findAccessToken(token: String): Future[Option[AccessToken]] =
    findUserByAccessToken(token).map(_.flatMap(_.accessToken))

  def findUserByAccessToken(token: String): Future[Option[User]] =
    getUsersCollection.flatMap(_.find(Filters.equal("accessToken", token)).first().toFutureOption())

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] =
    findUserByAccessToken(accessToken.token).map { opt =>
      opt.map { user =>
        AuthInfo[User](
          user = user,
          clientId = None,
          scope = None,
          redirectUri = None
        )
      }
    }

}
