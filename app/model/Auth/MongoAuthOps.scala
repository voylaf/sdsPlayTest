package model.Auth

import model.MongoDBActions
import org.mongodb.scala.model.{Filters, Updates}
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}
import play.api.Logging
import scalaoauth2.provider.{AccessToken, AuthInfo}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoAuthOps @Inject() (
    connectionString: String,
    dbName: String,
    usersCollection: String
)(implicit ec: ExecutionContext) extends OAuthOps {
  def mongoClient: MongoClient = MongoDBActions.clientFromConnectionString(connectionString)

  def getDatabase: Future[MongoDatabase] = MongoDBActions(mongoClient).getDatabase(dbName)

  def getUsersCollection: Future[MongoCollection[User]] = getDatabase.map(md => md.getCollection(usersCollection))

  def addUser(user: User): Future[Unit] = for {
    users <- getUsersCollection
    _     <- users.insertOne(user).toFuture()
  } yield ()

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

  def findUserByAccessToken(token: String): Future[Option[User]] = {
    for {
      users     <- getUsersCollection
      maybeUser <- users.find(Filters.eq("accessToken.token", token)).first().toFutureOption()
    } yield maybeUser
  }

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = {
    for {
      users       <- getUsersCollection
      maybeUser   <- users.find(Filters.eq("_id", authInfo.user._id)).first().toFutureOption()
      user        <- Future(maybeUser.get)
      accessToken <- Future(user.accessToken)
    } yield accessToken
  }

  def saveAccessToken(user: User, accessToken: AccessToken): Future[Unit] = for {
    users <- getUsersCollection
    res   <- users.updateOne(Filters.equal("_id", user._id), Updates.set("accessToken", accessToken)).toFutureOption()
  } yield ()

  def deleteAccessToken(user: User): Future[Unit] =
    getUsersCollection.flatMap(users =>
      users.updateOne(
        Filters.equal("_id", user._id),
        Updates.unset("accessToken")
      ).toFutureOption()
    ).map(_ => ())

  def deleteUsersCollection(): Future[Unit] =
    getUsersCollection.flatMap { _.deleteMany(Filters.notEqual("name", "")).toFuture().map(_ => ()) }

}
