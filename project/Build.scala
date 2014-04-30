import sbt._
import Keys._

object SbtMultiBuild extends Build {
    lazy val parent = Project(id = "bts", base = file(".")) aggregate(common, crawl)
    lazy val common = Project(id = "bts-lib-common", base = file("lib-common"))
    lazy val crawl = Project(id = "bts-crawl", base = file("crawl-framework")).dependsOn(common % "compile->package")
}
