name := "BTS-lib-common"

version := "0.1"

organization := "ch.epfl"

scalaVersion := "2.10.3"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
	"com.github.nscala-time" %% "nscala-time" % "0.8.0"
)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
