name := "Bitcoin Trading System"

version := "0.1"

scalaVersion := "2.10.3"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
"org.scalatest" % "scalatest_2.10" % "2.1.0" % "test",
"org.apache.httpcomponents" % "fluent-hc" % "4.3.3",
"com.github.nscala-time" %% "nscala-time" % "0.8.0",
"junit" % "junit" % "4.11" % "test",
"com.novocode" % "junit-interface" % "0.9" % "test->default",
"org.mockito" % "mockito-core" % "1.9.5",
"org.specs2" % "specs2_2.10" % "2.3.10" % "test",
"net.liftweb" %% "lift-json" % "2.5",
"com.typesafe.akka" %% "akka-actor" % "2.3.1",
"org.twitter4j" % "twitter4j-stream" % "3.0.3"
)
