package model

import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates
import org.mongodb.scala.{MongoCollection, MongoDatabase}

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

  def modifyStudentFields(studentId: Id, su: StudentUpdate): Future[Option[Student]] = {
    val us      = su.updates.collect { case (k, v) if k != "_id" => Updates.set(k, v) }.toList
    val updates = Updates.combine(us: _*)
    for {
      collection <- getStudentsCollection
      _ <- collection.updateOne(equal("_id", studentId), updates).toFuture()
      student <- findStudentById(studentId)
    } yield student
  }

  def addStudent(student: Student): Future[Unit] = {
    getStudentsCollection.flatMap(_.insertOne(student).toFuture()).map(_ => ())
  }

  def deleteStudent(studentId: Id): Future[String] =
    getStudentsCollection.flatMap(_.deleteOne(equal("_id", studentId)).toFuture()).map(dr =>
      s"Deleted count: ${dr.getDeletedCount}"
    )

  def replaceStudent(student: Student): Future[Unit] =
    getStudentsCollection.flatMap(
      _.replaceOne(equal("_id", student._id), student).toFuture()
    ).map(_ => ())
}
