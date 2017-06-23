organization in ThisBuild := "com.optrak"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.11"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `test-akka-integration` = (project in file("."))
  .aggregate(`persistent-api`, `persistent-impl`)

lazy val `persistent-api` = (project in file("persistent/api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `persistent-impl` = (project in file("persistent/impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`persistent-api`)
  .dependsOn(`utils`)

lazy val `utils` = (project in file("utils"))
  .settings(
    libraryDependencies ++= Seq(
      macwire,
      scalaTest,
      lagomScaladslApi
    )
  )
  .settings(lagomForkedTestSettings: _*)


lazy val `simple-api` = (project in file("akka-model/simple/api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`persistent-api`)

lazy val `model-utils` = (project in file("akka-model/utils"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`persistent-api`)

lazy val `simple-impl` = (project in file("akka-model/simple/impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`simple-api`)
  .dependsOn(`model-utils`)

lazy val `wired-api` = (project in file("akka-model/wired/api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`persistent-api`)

lazy val `wired-impl` = (project in file("akka-model/wired/impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`model-utils`)
  .dependsOn(`wired-api`)



