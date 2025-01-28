ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(
    name         := """playTest""",
    organization := "com.github.voylaf",
    libraryDependencies ++= List(
      guice,
      "org.mongodb.scala"      %% "mongo-scala-driver" % "5.3.1",
      "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
      "org.scalameta" %% "munit" % "1.1.0" % Test,
    )
  )

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.github.voylaf.binders._"
