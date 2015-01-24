
import ScalaxbKeys._
import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import de.johoop.cpd4sbt.CopyPasteDetector._

organization := "com.github.peregin"

name := "telemetry-on-video"

version := "1.0.0-SNAPSHOT"

mainClass in Compile := Some("peregin.gpv.App")

scalaVersion := "2.10.4"

scalacOptions ++= List("-target:jvm-1.6", "-feature", "-deprecation", "-language:implicitConversions")

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)

resolvers ++= Seq(
  "Xuggle Repo" at "http://xuggle.googlecode.com/svn/trunk/repo/share/java/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

cpdSettings

net.virtualvoid.sbt.graph.Plugin.graphSettings

scalaxbSettings

sourceGenerators in Compile <+= scalaxb in Compile

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") {
  new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date())
})

buildInfoPackage := "info"

packageArchetype.java_application

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.4"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.7"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.7"

libraryDependencies += "org.swinglabs" % "swingx-core" % "1.6.2-2"

libraryDependencies += "org.swinglabs" % "swingx-ws" % "1.0"

libraryDependencies += "com.jgoodies" % "looks" % "2.2.2"

libraryDependencies += "com.jgoodies" % "jgoodies-common" % "1.7.0"

libraryDependencies += "com.miglayout" % "miglayout" % "3.7.4"

libraryDependencies += "xuggle" % "xuggle-xuggler" % "5.4"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.11"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.0"

libraryDependencies += "joda-time" % "joda-time" % "2.5"

libraryDependencies += "org.joda" % "joda-convert" % "1.7"

libraryDependencies += "org.specs2" %% "specs2" % "2.4.2" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit"  % "2.3.7" % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.10" % "test"

publishTo := Some(Resolver.file("peregin@github", file(Path.userHome + "/data/repo")))
