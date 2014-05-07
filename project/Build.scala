import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._


object SbtMultiBuild extends Build {
    scalaVersion := "2.10.3"
    lazy val app = Project(id = "bts", base = file(".")) .dependsOn(commons) aggregate(crawler, webui)
    lazy val commons = Project(id = "bts-commons", base = file("commons"))
    lazy val crawler = Project(id = "bts-crawler", base = file("crawl-framework")).dependsOn(commons)
    lazy val webui = Project(id = "bts-webui", base = file("reactive-stocks")).dependsOn(commons)
}
