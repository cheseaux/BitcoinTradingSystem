name := """reactive-stocks"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.2.1",
  "org.webjars" %% "webjars-play" % "2.2.1",
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.1" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "ch.epfl" %% "bts-lib-common" % "0.1"
)

resolvers += (
    "lib-common resolver" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
)



play.Project.playScalaSettings
