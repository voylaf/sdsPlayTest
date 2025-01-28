package model

trait DBActions[F[_], DB] {
  def createDatabase(dbName: String): F[DB]

  //has to return token
  def auth(dbName: String): F[Unit]

}
