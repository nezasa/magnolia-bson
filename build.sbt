name := "magnolia-bson"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.12.10", "2.13.1")

libraryDependencies += "com.propensive" %% "magnolia" % "0.12.6"
libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson" % "0.12.6"

//scalacOptions += "-Xlog-implicits"
//scalacOptions += "-Ymacro-debug-lite"
