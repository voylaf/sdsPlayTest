import org.bson.types.ObjectId

package object model {
  //MongoDb's ObjectId or UUID
  type Id = ObjectId
  type InMemoryDB = scala.collection.mutable.Map[Id, Student]
}
