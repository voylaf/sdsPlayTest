package model

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class MongoDBActions(mongoClient: MongoClient) extends DBActions[Future, MongoDatabase] {

  private val codecRegistry = fromRegistries(
    fromProviders(classOf[Student]),
    DEFAULT_CODEC_REGISTRY
  )

  def createDatabase(dbName: String): Future[MongoDatabase] =
    Future(mongoClient.getDatabase(dbName))
      .map(_.withCodecRegistry(codecRegistry))

  def getDatabase(dbName: String): Future[MongoDatabase] =
    Future(mongoClient.getDatabase(dbName))
      .map(_.withCodecRegistry(codecRegistry))

  def createCollection(collectionName: String, mongoDatabase: MongoDatabase): Future[Unit] =
    mongoDatabase.createCollection(collectionName).toFuture()

  def auth(dbName: String): Future[Unit] = Future.unit
}

object MongoDBActions {
  def fromConnectionString(connectionString: String): MongoDBActions = {
    val settings: MongoClientSettings = MongoClientSettings
      .builder()
      .applyConnectionString(ConnectionString(connectionString))
      .build()
    val mongoClient: MongoClient       = MongoClient(settings)
    MongoDBActions(mongoClient)
  }
}
