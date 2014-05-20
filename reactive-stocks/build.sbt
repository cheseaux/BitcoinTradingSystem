import sbt._
import Process._
import Keys._
import play.Project._

name := "BTS-webui"

version := "0.1"

organization := "ch.epfl.bigdata.coin"

scalaVersion := "2.10.3"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-remote" % "2.2.1",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.1",
  "org.webjars" %% "webjars-play" % "2.2.1",
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "ch.epfl.bigdata.coin" %% "bts-commons" % "0.1-SNAPSHOT",
  "org.json" % "json" % "20140107"
)

resolvers += (
    "lib-common resolver" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)

play.Project.playScalaSettings
