package model

import model.StudentImpl.studentFormat
import model.StudentImpl.objectIdFormat
import org.bson.types.ObjectId
import play.api.libs.json._

class StudentSpec extends munit.FunSuite {
  test("student from json") {
    val student     = Student("Vasilyev", "Alexey", "Ignatyevich", "a6", 4.5)
    val studentJson = Json.toJson[Student](student)
//    val studentWoId = Json.parse(
//      """{
//        |"surname":"Vasilyev",
//        |"name":"Alexey",
//        |"patronym":"Ignatyevich",
//        |"group":"a6",
//        |"avgScore":4.5,
//        |"_id":"507f191e810c19729de860ea"}
//        |""".stripMargin
//    )
    val studentFromJsonRes = Json.fromJson[Student](studentJson)
    assert(studentFromJsonRes.isSuccess, s"from json is failed $studentFromJsonRes")
    assert(student == studentFromJsonRes.get, "students are not equal")
  }

}
