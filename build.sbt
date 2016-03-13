name := """Play"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "org.scalatest" % "scalatest_2.11" % "3.0.0-M15" % Test,
  "org.scalatestplus" % "play_2.11" % "1.4.0" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.13" % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator
