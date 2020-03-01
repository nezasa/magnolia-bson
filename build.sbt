ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / crossScalaVersions := Seq("2.12.8", "2.13.0")
ThisBuild / organization     := "com.github.nezasa"
ThisBuild / organizationName := "nezasa"
ThisBuild / organizationHomepage := Some(url("https://github.com/nezasa/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/nezasa/mangnolia-bson/"),
    "scm:git@github.com:nezasa/mangnolia-bson.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "nezasadev",
    name  = "Nezasa Devs",
    email = "dev@nezasa.com",
    url   = url("http://your.url")
  )
)


ThisBuild / homepage := Some(url("https://github.com/nezasa/mangnolia-bson"))
ThisBuild / licenses += ("MIT", url("http://opensource.org/licenses/MIT"))


name := "magnolia-bson"

libraryDependencies += "com.propensive" %% "magnolia" % "0.12.6"
libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson" % "0.20.3"
libraryDependencies += "org.specs2" %% "specs2-core" % "4.7.0"

//scalacOptions += "-Xlog-implicits"
//scalacOptions += "-Ymacro-debug-lite"
