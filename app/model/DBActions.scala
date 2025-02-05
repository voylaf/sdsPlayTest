package model

trait DBActions[F[_], DB] {
  def createDatabase(dbName: String): F[DB]

  def getDatabase(dbName: String): F[DB]
}
