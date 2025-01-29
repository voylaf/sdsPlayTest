package model

import org.mongodb.scala.bson.ObjectId

final case class Student(surname: String, name: String, patronym: String, group: String, avgRate: Double, _id: ObjectId = new ObjectId())
