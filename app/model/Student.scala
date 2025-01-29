package model

import org.mongodb.scala.bson.ObjectId

final case class Student(surname: String, name: String, patronym: String, group: String, avgScore: Double, _id: ObjectId = new ObjectId()) {
  override def toString = s"$surname $name $patronym is a part of group $group and has score $avgScore"
}
