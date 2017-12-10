
organization := "com.github.peregin"

name := "telemetry-on-video"

version := "1.0.0-SNAPSHOT"

mainClass in Compile := Some("peregin.gpv.App")

scalaVersion := "2.12.4"

scalacOptions ++= List("-target:jvm-1.8", "-feature", "-deprecation", "-language:implicitConversions", "-language:reflectiveCalls")

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)

resolvers ++= Seq(
  "Xuggle Repo" at "http://xuggle.googlecode.com/svn/trunk/repo/share/java/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

val json4sVersion = "3.5.3"
val akkaVersion = "2.5.8"
val specs2Version = "4.0.2"
val logbackVersion = "1.2.3"
val batikVersion = "1.9.1" // svg manipulation

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin, JavaAppPackaging).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") {
      new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date())
    }),
    buildInfoPackage := "info"
  )

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

libraryDependencies += "org.swinglabs" % "swingx-core" % "1.6.2-2"

libraryDependencies += "org.swinglabs" % "swingx-ws" % "1.0"

libraryDependencies += "com.jgoodies" % "looks" % "2.2.2"

libraryDependencies += "com.jgoodies" % "jgoodies-common" % "1.8.1"

libraryDependencies += "com.miglayout" % "miglayout" % "3.7.4"

libraryDependencies += "xuggle" % "xuggle-xuggler" % "5.4" from "https://files.liferay.com/mirrors/xuggle.googlecode.com/svn/trunk/repo/share/java/xuggle/xuggle-xuggler/5.4/xuggle-xuggler-5.4.jar"

libraryDependencies += "org.json4s" %% "json4s-native" % json4sVersion

libraryDependencies += "org.json4s" %% "json4s-jackson" % json4sVersion

libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion

libraryDependencies += "ch.qos.logback" % "logback-core" % logbackVersion

libraryDependencies += "joda-time" % "joda-time" % "2.9.9"

libraryDependencies += "org.joda" % "joda-convert" % "1.9.2"

libraryDependencies += "org.apache.xmlgraphics" % "batik-transcoder" % batikVersion

libraryDependencies += "com.google.guava" % "guava" % "23.0"

libraryDependencies += "org.specs2" %% "specs2-core" % specs2Version % "test"

libraryDependencies += "org.specs2" %% "specs2-scalacheck" % specs2Version % "test"

libraryDependencies += "org.specs2" %% "specs2-mock" % specs2Version % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit"  % akkaVersion % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"

publishTo := Some(Resolver.file("peregin@github", file(Path.userHome + "/data/repo")))
