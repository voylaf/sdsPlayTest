package model

import org.mongodb.scala.{ConnectionString, Document, MongoClient, MongoClientSettings}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Using

object MongoClientExample {
  def main(args: Array[String]): Unit = run()

  def run(): Unit = {
    val connectionString = "mongodb://localhost:27017"
    val settings = MongoClientSettings
      .builder()
      .applyConnectionString(ConnectionString(connectionString))
      .build()
    Using(MongoClient(settings)) { mongoClient =>
      // Send a ping to confirm a successful connection
      val database = mongoClient.getDatabase("admin")
      val ping = database.runCommand(Document("ping" -> 1)).head()
      Await.result(ping, 10.seconds)
      System.out.println("Pinged your deployment. You successfully connected to MongoDB!")
    }.recover(e => e.printStackTrace())
  }
}
