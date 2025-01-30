package model

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.ObservableImplicits
import org.mongodb.scala.model.Updates
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.{ExecutionContext, Future}

final case class StudentActionsMongoDB(mongoDb: MongoDatabase, collectionName: String)(implicit ec: ExecutionContext)
    extends StudentActions[Future] {
  def getStudentsCollection: Future[MongoCollection[Student]] =
    Future(mongoDb.getCollection(collectionName))

  def getStudentsList: Future[Seq[Student]] =
    getStudentsCollection.flatMap(_.find().toFuture())

  def findStudentById(id: Id): Future[Option[Student]] = {
    getStudentsCollection.flatMap(
      _.find(equal("_id", id))
        .first()
        .toFuture()
    ).map(Option(_))
      .recover(_ => None)
  }

  def modifyStudentFields(studentId: Id, kv: List[(String, Any)]): Future[Unit] = {
    val updates = kv.foldLeft(Updates.combine()) { case (acc, (k, v)) =>
      Updates.combine(acc, Updates.set(k, v))
    }
    getStudentsCollection.flatMap(col =>
      col.updateOne(equal("_id", studentId), updates).toFuture()
    )
  }.map(_ => ())

  def addStudent(student: Student): Future[Unit] = {
    getStudentsCollection.flatMap(_.insertOne(student).toFuture()).map(_ => ())
  }

  def deleteStudent(student: Student): Future[Unit] =
    getStudentsCollection.flatMap(_.deleteOne(equal("_id", student._id)).toFuture()).map(_ => ())

  def replaceStudent(student: Student): Future[Unit] =
    getStudentsCollection.flatMap(
      _.replaceOne(equal("_id", student._id), student).toFuture()
    ).map(_ => ())
}
