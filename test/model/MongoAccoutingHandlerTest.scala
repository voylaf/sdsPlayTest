package model

import model.Auth.{MongoAccountingHandler, User}
import model.Auth.User.hashString
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters
import scalaoauth2.provider.AuthInfo

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class MongoAccoutingHandlerTest extends munit.FunSuite {
  val mongoClient: MongoDBActions = MongoDBActions.fromConnectionString("mongodb://localhost:27017")
  val mongoAccoutingHandler       = new MongoAccountingHandler("mongodb://localhost:27017", "TestEx", "Users")(60)
  val newId                       = new ObjectId()
  val password                    = "MyPassword"
  val user: User                  = User(newId, "Test", hashString(password))
  val authInfo: AuthInfo[User]    = AuthInfo(user = user, clientId = None, scope = None, redirectUri = None)

  override def beforeAll(): Unit = {
    Await.result(mongoAccoutingHandler.addUser(user), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(mongoAccoutingHandler.getUsersCollection.flatMap(_.drop().toFuture()), Duration.Inf)
  }

  test("must add user") {
    for {
      users <- mongoAccoutingHandler.getUsersCollection
      count <- users.countDocuments(
        Filters.equal("_id", newId)
      ).toFuture()
    } yield {
      assertEquals(count, 1L)
    }
  }

  test("must find user") {
    for {
      maybeUser <- mongoAccoutingHandler.findUserByNameAndPassword(user.name, password)
    } yield {
      assert(maybeUser.nonEmpty)
      assertEquals(maybeUser.get, user)
    }
  }

  test("must create access token") {
    for {
      accessToken <- mongoAccoutingHandler.createAccessToken(authInfo)
      ur <- mongoAccoutingHandler.saveAccessToken(user, accessToken)
      users <- mongoAccoutingHandler.getUsersCollection
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
      accessToken <- mongoAccoutingHandler.getStoredAccessToken(authInfo)
      dr <- mongoAccoutingHandler.deleteAccessToken(user)
      deletedToken <- mongoAccoutingHandler.getStoredAccessToken(authInfo)
    } yield {
      assert(accessToken.nonEmpty)
      assert(deletedToken.isEmpty)
    }
  }

}
