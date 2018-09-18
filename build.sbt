import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease._
import ReleaseStateTransformations._

organization := "com.github.peregin"

name := "telemetry-on-video"

val entryPoint = "peregin.gpv.App"

mainClass in Compile := Some(entryPoint)
mainClass in assembly := Some(entryPoint)

scalaVersion := "2.12.6"

scalacOptions ++= List("-target:jvm-1.8", "-feature", "-deprecation", "-language:implicitConversions", "-language:reflectiveCalls")

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)

resolvers ++= Seq(
  "Xuggle Repo" at "http://xuggle.googlecode.com/svn/trunk/repo/share/java/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
assemblyJarName in assembly := "gps-overlay-on-video.jar"

val json4sVersion = "3.5.4"
val akkaVersion = "2.5.16"
val specs2Version = "4.3.4"
val logbackVersion = "1.2.3"
val batikVersion = "1.10" // svg manipulation

lazy val releaseToGh: ReleaseStep = ReleaseStep(
  action = { st: State =>
    val extracted = Project.extract(st)
    extracted.runInputTask(githubRelease, "", st)._1
  }
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") {
      new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date())
    }),
    buildInfoPackage := "info",
    ghreleaseRepoOrg := "peregin",
    ghreleaseRepoName := "gps-overlay-on-video",
    ghreleaseNotes := (v => s"Release $v"),
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,              // : ReleaseStep
      inquireVersions,                        // : ReleaseStep
      runClean,                               // : ReleaseStep
      runTest,                                // : ReleaseStep
      setReleaseVersion,                      // : ReleaseStep
      commitReleaseVersion,                   // : ReleaseStep, performs the initial git checks
      tagRelease,                             // : ReleaseStep
      releaseToGh,                       // : ReleaseStep, checks whether `publishTo` is properly set up
      setNextVersion,                         // : ReleaseStep
      commitNextVersion,                      // : ReleaseStep
      pushChanges                             // : ReleaseStep, also checks that an upstream branch is properly configured
    )
  )

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.3"

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

libraryDependencies += "joda-time" % "joda-time" % "2.10"

libraryDependencies += "org.joda" % "joda-convert" % "2.1.1"

libraryDependencies += "org.apache.xmlgraphics" % "batik-transcoder" % batikVersion

libraryDependencies += "com.google.guava" % "guava" % "23.0"

libraryDependencies += "org.specs2" %% "specs2-core" % specs2Version % "test"

libraryDependencies += "org.specs2" %% "specs2-scalacheck" % specs2Version % "test"

libraryDependencies += "org.specs2" %% "specs2-mock" % specs2Version % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit"  % akkaVersion % "test"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"
