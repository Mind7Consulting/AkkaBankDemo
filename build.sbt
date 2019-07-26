name := "AkkaBank"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.6.0-M1"
lazy val cassandraPluginVersion = "0.97"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "com.typesafe" % "config" % "1.3.4",
  "org.slf4j" % "slf4j-api" % "1.7.24",
  "org.postgresql" % "postgresql" % "42.2.5",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
