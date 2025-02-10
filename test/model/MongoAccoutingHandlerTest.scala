package model

import model.Auth.{UsersAccountingHandler, MongoAuthOps, User}
import model.Auth.User.hashString
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters
import scalaoauth2.provider.AuthInfo

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class MongoAccoutingHandlerTest extends munit.FunSuite {
  val mongoClient: MongoDBActions = MongoDBActions.fromConnectionString("mongodb://localhost:27017")
  val mongoAuthOps = new MongoAuthOps("mongodb://localhost:27017", "TestEx", "Users")
  val mongoAccoutingHandler       = new UsersAccountingHandler(mongoAuthOps, 60)
  val newId                       = new ObjectId()
  val password                    = "MyPassword"
  val user: User                  = User(newId, "Test", hashString(password))
  val authInfo: AuthInfo[User]    = AuthInfo(user = user, clientId = None, scope = None, redirectUri = None)

  override def beforeAll(): Unit = {
    Await.result(mongoAuthOps.addUser(user), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(mongoAuthOps.deleteUsersCollection(), Duration.Inf)
  }

  test("must add user") {
    for {
      users <- mongoAuthOps.getUsersCollection
      count <- users.countDocuments(
        Filters.equal("_id", newId)
      ).toFuture()
    } yield {
      assertEquals(count, 1L)
    }
  }

  test("must find user") {
    for {
      maybeUser <- mongoAuthOps.findUserByNameAndPassword(user.name, password)
    } yield {
      assert(maybeUser.nonEmpty)
      assertEquals(maybeUser.get, user)
    }
  }

  test("must create access token") {
    for {
      accessToken <- mongoAccoutingHandler.createAccessToken(authInfo)
      ur <- mongoAuthOps.saveAccessToken(user, accessToken)
      users <- mongoAuthOps.getUsersCollection
      count <- users.countDocuments(
        Filters.and(Filters.ne("accessToken", null), Filters.equal("_id", newId))
      ).toFuture()
    } yield {
      assert(!accessToken.isExpired)
      assertEquals(count, 1L)
    }
  }

  test("must delete access token") {
    for {
      accessToken <- mongoAuthOps.getStoredAccessToken(authInfo)
      dr <- mongoAuthOps.deleteAccessToken(user)
      deletedToken <- mongoAuthOps.getStoredAccessToken(authInfo)
    } yield {
      assert(accessToken.nonEmpty)
      assert(deletedToken.isEmpty)
    }
  }

}
