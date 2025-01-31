package model

import org.bson.types.ObjectId
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{Format, JsNumber, JsString, JsValue, Json, Reads, Writes}

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

final case class StudentUpdate(
    surname: Option[String] = None,
    name: Option[String] = None,
    patronym: Option[String] = None,
    group: Option[String] = None,
    avgScore: Option[Double] = None,
    _id: Option[ObjectId] = None
) {
  val updates: Map[String, Any] = Map(
    "surname"  -> surname,
    "name"     -> name,
    "patronym" -> patronym,
    "group"    -> group,
    "avgScore" -> avgScore,
    "_id"      -> _id
  ).collect { case (k, Some(v)) => (k, v) }
  val updatesJson: Map[String, JsValueWrapper] = updates.map {
    case (k, v: String) => (k, JsString(v))
    case (k, v: Double) => (k, JsNumber(v))
    case (k, v: ObjectId) => (k, JsString(v.toString))
  }
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

  implicit val studentUpdateFormat: Format[StudentUpdate] = Format(
    Reads[StudentUpdate] {
      case jsObj: JsObject =>
        JsSuccess(StudentUpdate(
          surname = (jsObj \ "surname").toOption.map(_.as[String]),
          name = (jsObj \ "name").toOption.map(_.as[String]),
          patronym = (jsObj \ "patronym").toOption.map(_.as[String]),
          group = (jsObj \ "group").toOption.map(_.as[String]),
          avgScore = (jsObj \ "avgScore").toOption.flatMap {
            case s: JsString => Some(s.as[String].toDouble)
            case d: JsNumber => Some(d.as[Double])
            case _           => None
          },
          _id = (jsObj \ "_id").toOption.map(_.as[ObjectId])
        ))
      case _ => JsError()
    },
    Writes[StudentUpdate] { (su: StudentUpdate) =>
      Json.obj(su.updatesJson.toList: _*)
    }
  )
}
