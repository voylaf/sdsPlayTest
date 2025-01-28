package model

import org.mongodb.scala.bson.ObjectId

//object Student {
//  def apply(surname: String, name: String, patronym: String, group: String, avgRate: Float): Student =
//    Student(new ObjectId(), surname, name, patronym, group, avgRate)
//}

final case class Student(surname: String, name: String, patronym: String, group: String, avgRate: Double, _id: ObjectId = new ObjectId())
