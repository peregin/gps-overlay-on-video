import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import ScalaxbKeys._

organization := "peregin.com"

name := "telemetry-on-video"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= List("-target:jvm-1.6", "-feature", "-deprecation", "-language:implicitConversions")

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)

resolvers ++= Seq(
  "Xuggle Repo" at "http://xuggle.googlecode.com/svn/trunk/repo/share/java/"
)

net.virtualvoid.sbt.graph.Plugin.graphSettings

scalaxbSettings

sourceGenerators in Compile <+= scalaxb in Compile

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") {
  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
})

buildInfoPackage := "info"

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.3"

libraryDependencies += "org.swinglabs" % "swingx-core" % "1.6.2-2"

libraryDependencies += "org.swinglabs" % "swingx-ws" % "1.0"

libraryDependencies += "com.jgoodies" % "looks" % "2.2.2"

libraryDependencies += "com.jgoodies" % "jgoodies-common" % "1.7.0"

libraryDependencies += "com.miglayout" % "miglayout" % "3.7.4"

libraryDependencies += "xuggle" % "xuggle-xuggler" % "5.4"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.8"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.8"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.0"

libraryDependencies += "joda-time" % "joda-time" % "2.3"

libraryDependencies += "org.joda" % "joda-convert" % "1.6"

libraryDependencies += "org.specs2" %% "specs2" % "2.3.10" % "test"

