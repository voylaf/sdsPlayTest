package model

import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoDatabase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class MongoDBActions(mongoClient: MongoClient) extends DBActions[Future, MongoDatabase] {

  private val codecRegistry = fromRegistries(
    fromProviders(classOf[Student], classOf[StudentUpdate]),
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
  def fromConnectionString(connectionString: String): MongoDBActions = {
    val settings: MongoClientSettings = MongoClientSettings
      .builder()
      .applyConnectionString(ConnectionString(connectionString))
      .build()
    val mongoClient: MongoClient = MongoClient(settings)
    MongoDBActions(mongoClient)
  }
}
