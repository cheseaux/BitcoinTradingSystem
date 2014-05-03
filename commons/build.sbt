import sbt._
import Process._
import Keys._

name := "BTS-commons"

version := "0.1-SNAPSHOT"

organization := "ch.epfl.bigdata.coin"

scalaVersion := "2.10.3"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
	"com.github.nscala-time" %% "nscala-time" % "0.8.0"
)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
