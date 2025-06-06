package model

trait StudentActions[F[_]] {
  def getStudentsList: F[Seq[Student]]

  def findStudentById(id: Id): F[Option[Student]]

  def replaceStudent(student: Student): F[Unit]

  def modifyStudentFields(studentId: Id, kv: StudentUpdate): F[Option[Student]]

  def addStudent(student: Student): F[Unit]

  def deleteStudent(studentId: Id): F[String]
}
