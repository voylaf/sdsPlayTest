package model

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class MongoDBActionsSpec extends munit.FunSuite {
  val connectionString = "mongodb://localhost:27017"
  val mongoDBActions: MongoDBActions = MongoDBActions.fromConnectionString(connectionString)

  val students: List[Student] = List(
    Student("Ivanov", "Petr", "Alexandrovich", "u98", 3.21),
    Student("Gorlov", "Gavr", "Maksimovich", "u96", 3.09)
  )

  test("createAndGet") {
    val dbName     = "TestEx"
    val database   = Await.result(mongoDBActions.createDatabase(dbName), 5.seconds)
    val returnedDb = Await.result(mongoDBActions.getDatabase(dbName), 5.seconds)
    Await.result(mongoDBActions.createCollection("Students", returnedDb), 5.seconds)

    val studentActions = StudentActionsMongoDB(database, "Students")

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

    val student2 = Await.result(
      for {
        _       <- studentActions.addStudent(students(1))
        student <- studentActions.findStudentById(students(1)._id)
      } yield student,
      5.seconds
    ).get
    Await.result(studentActions.deleteStudent(student2._id), 5.seconds)
    val student2Lack2 = Await.result(studentActions.findStudentById(students(1)._id), 5.seconds)
    assert(student2Lack2.isEmpty, "DB has the student, which was deleted")

    val changes = StudentUpdate(group=Some("u99"), avgScore=Some(3.67))
    val student3 = Await.result(
      for {
        student       <- studentActions.modifyStudentFields(students.head._id, changes)
//        student <- studentActions.findStudentById(students.head._id)
      } yield student,
      5.seconds
    )
    assert(student3.nonEmpty, "DB must have the changed student")
    val student3Changed = students.head.copy(group = "u99", avgScore = 3.67)
    assert(student3.get == student3Changed, "DB must see update")

    val studentForReplace = student3Changed.copy(avgScore = 4.12, surname = "Przybyszewski")
    val student4 = Await.result(
      for {
        _       <- studentActions.replaceStudent(studentForReplace)
        student <- studentActions.findStudentById(studentForReplace._id)
      } yield student,
      5.seconds
    )
    assert(student4.nonEmpty, "DB must have the replaced student")
    assert(student4.get == studentForReplace, "DB must see replace")

  }

}
