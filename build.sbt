name := "akkahttp-quickstart"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.22"
val akkaHttpVersion = "10.1.8"
val circeVersion = "0.9.3"


libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.8"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.19" // or whatever the latest version is
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"