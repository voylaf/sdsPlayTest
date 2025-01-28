package model

import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class MongoDBActionsSpec extends munit.FunSuite {
  val connectionString = "mongodb://localhost:27017"
  val settings: MongoClientSettings = MongoClientSettings
    .builder()
    .applyConnectionString(ConnectionString(connectionString))
    .build()
  val mongoClient: MongoClient       = MongoClient(settings)
  val mongoDBActions: MongoDBActions = MongoDBActions(mongoClient)

  val students: List[Student] = List(
    Student("Ivanov", "Petr", "Alexandrovich", "u98", 3.21),
    Student("Gorlov", "Gavr", "Maksimovich", "u96", 3.09)
  )

  test("createAndGet") {
    val dbName     = "TestEx"
    val database   = Await.result(mongoDBActions.createDatabase(dbName), 5.seconds)
    val returnedDb = Await.result(mongoDBActions.getDatabase(dbName), 5.seconds)
    Await.result(mongoDBActions.createCollection("Students", returnedDb), 5.seconds)

    val studentActions = StudentActionsMongoDB(database)

    val student1 = Await.result(
      for {
        _       <- studentActions.addStudent(students.head)
        student <- studentActions.findStudentById(students.head._id)
      } yield student,
      5.seconds
    )
    val student2Lack = Await.result(studentActions.findStudentById(students(1)._id), 5.seconds)
    assert(student1.nonEmpty, "DB doesn't have the added student")
    assert(student1.get == students.head, "The added student must be the same after reading")
    assert(student2Lack.isEmpty, "DB has the student, which wasn't added")
  }

}
