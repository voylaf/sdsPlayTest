package model

import org.bson.types.ObjectId

final case class Student(
    surname: String,
    name: String,
    patronym: String,
    group: String,
    avgScore: Double,
    _id: ObjectId = new ObjectId()
) {
  def show = s"$surname $name $patronym (${_id}) is a part of the group $group and has score $avgScore"
}

object StudentImpl {
  import play.api.libs.json._
  implicit val objectIdFormat: Format[ObjectId] = Format(
    Reads[ObjectId] {
      case s: JsString if ObjectId.isValid(s.value) =>
        JsSuccess(new ObjectId(s.value))
      case _ => JsError()
    },
    Writes[ObjectId]((o: ObjectId) => JsString(o.toHexString))
  )

  implicit val studentFormat: Format[Student] = Json.format[Student]
}
