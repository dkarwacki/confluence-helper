import sbtassembly.PathList

name := "confluence-helper"
organization := "com.softwaremill"
version := "0.1"
scalaVersion := "2.13.6"

lazy val catsVersion = "2.6.1"
lazy val catsEffectVersion = "3.2.9"
lazy val fs2Version = "3.2.2"
lazy val sttpVersion = "3.3.16"
lazy val circeVersion = "0.14.1"
lazy val pureConfigVersion = "0.17.0"
lazy val logbackVersion = "1.2.3"
lazy val catsLoggerVersion = "2.1.1"

lazy val catsDependencies = Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.typelevel" %% "cats-effect-laws" % catsEffectVersion % Test
)

lazy val sttpDependencies = Seq(
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion
)

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion
)

lazy val pureConfigDependencies = Seq(
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
)

lazy val loggerDependencies = Seq(
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion
)

lazy val fs2Dependencies = Seq(
  "co.fs2" %% "fs2-core" % fs2Version
)

lazy val catsLoggerDependencies = Seq(
  "org.typelevel" %% "log4cats-core" % catsLoggerVersion,
  "org.typelevel" %% "log4cats-slf4j" % catsLoggerVersion
)

lazy val root = (project in file("."))
  .settings(
    scalacOptions ++= Seq(
      "-Ymacro-annotations"
    ),
    libraryDependencies ++=
      catsDependencies
        ++ sttpDependencies
        ++ circeDependencies
        ++ pureConfigDependencies
        ++ loggerDependencies
        ++ fs2Dependencies
        ++ catsLoggerDependencies
  )

assemblyJarName := "confluence-helper.jar"
assemblyMergeStrategy := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.discard
  case PathList("META-INF", "maven", "pom.properties") =>
    MergeStrategy.singleOrError
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
