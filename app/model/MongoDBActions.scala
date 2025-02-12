package model

import model.Auth.User
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoDatabase}
import scalaoauth2.provider.AccessToken

import scala.concurrent.{ExecutionContext, Future}

final case class MongoDBActions(mongoClient: MongoClient)(implicit ec: ExecutionContext) extends DBActions[Future, MongoDatabase] {

  private val codecRegistry = fromRegistries(
    fromProviders(classOf[Student], classOf[StudentUpdate], classOf[User], classOf[AccessToken]),
    DEFAULT_CODEC_REGISTRY
  )

  def createDatabase(dbName: String): Future[MongoDatabase] =
    this.getDatabase(dbName)

  def getDatabase(dbName: String): Future[MongoDatabase] =
    Future(mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry))

  def createCollection(collectionName: String, mongoDatabase: MongoDatabase): Future[Unit] =
    mongoDatabase.createCollection(collectionName).toFuture()
}

object MongoDBActions {
  def clientFromConnectionString(connectionString: String): MongoClient = {
    val settings: MongoClientSettings = MongoClientSettings
      .builder()
      .applyConnectionString(ConnectionString(connectionString))
      .build()
    MongoClient(settings)
  }
  def fromConnectionString(connectionString: String)(implicit ec: ExecutionContext): MongoDBActions =
    MongoDBActions(clientFromConnectionString(connectionString))
}
