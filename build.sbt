ThisBuild / version      := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(
    name         := """playTest""",
    organization := "com.github.voylaf",
    libraryDependencies ++= List(
      guice,
      ws,
      "org.mongodb.scala"      %% "mongo-scala-driver"    % "5.3.1",
      "com.nulab-inc"          %% "scala-oauth2-core"     % "1.6.0",
      "com.nulab-inc"          %% "play2-oauth2-provider" % "2.0.0",
      "org.scalatestplus.play" %% "scalatestplus-play"    % "7.0.1" % Test,
      "org.scalameta"          %% "munit"                 % "1.1.0" % Test
    )
  )

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.github.voylaf.binders._"
