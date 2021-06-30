import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease._
import ReleaseStateTransformations._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

organization := "com.github.peregin"
name := "telemetry-on-video"

val entryPoint = "peregin.gpv.GpsOverlayApp"

mainClass in Compile := Some(entryPoint)

scalaVersion := "2.13.4"

val json4sVersion = "3.6.11"
val akkaVersion = "2.6.14"
val specs2Version = "4.10.5"
val logbackVersion = "1.2.3"
val batikVersion = "1.13" // svg manipulation
val xmlVersion = "1.3.0"
val jodaVersion = "2.10.10"

scalacOptions ++= List("-target:jvm-1.8", "-feature", "-deprecation", "-language:implicitConversions", "-language:reflectiveCalls")
val macDockNameOpt = "-Xdock:name=\"GPS Overlay\""
javaOptions ++= List(macDockNameOpt)

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)
resolvers ++= Seq(
  "Xuggle Repo" at "https://xuggle.googlecode.com/svn/trunk/repo/share/java/",
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"
)

assembly / mainClass := Some(entryPoint)
assembly / assemblyJarName := "gps-overlay-on-video.jar"
assembly / assemblyOption := (assemblyOption in assembly).value.copy(prependShellScript = Some(defaultUniversalScript(javaOpts = Seq(macDockNameOpt), shebang = false)))
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.discard
  case PathList("junit", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
(assembly / test) := {}
artifact in(Compile, assembly) := {
  val art = (artifact in(Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}
addArtifact(artifact in(Compile, assembly), assembly)

publishArtifact := false // it is done by the assembly plugin

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin, AssemblyPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, BuildInfoKey.action("buildTime") {
      new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date())
    }),
    buildInfoPackage := "info",
    ghreleaseRepoOrg := "peregin",
    ghreleaseRepoName := "gps-overlay-on-video",
    ghreleaseNotes := (v => s"Release $v"),
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      ReleaseStep(releaseStepTask(assembly)),  // package artifacts
      pushChanges, // needed fot the GH plugin to use the latest tag
      ReleaseStep(releaseStepInputTask(githubRelease)),
      setNextVersion,
      commitNextVersion,
      pushChanges // push the next version
    )
  )

onLoadMessage := welcomeMessage.value

def welcomeMessage = Def.setting {
  import scala.Console

  def red(text: String): String = s"${Console.RED}$text${Console.RESET}"
  def item(text: String): String = s"${Console.GREEN}▶ ${Console.CYAN}$text${Console.RESET}"

  s"""|${red("""                                         """)}
      |${red("""          _                              """)}
      |${red(""" __ _____| |___  __ ___ _ _ _ _  ___ _ _ """)}
      |${red(""" \ V / -_) / _ \/ _/ _ \ '_| ' \/ -_) '_|""")}
      |${red("""  \_/\___|_\___/\__\___/_| |_||_\___|_|  """)}
      |${red("""                                         """+ version.value)}
      |
      |Useful sbt tasks:
      |${item("run")} - starts the application
      |${item("release")} - generates a new release
      """.stripMargin
}


libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.1.1"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % xmlVersion
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
libraryDependencies += "joda-time" % "joda-time" % jodaVersion
libraryDependencies += "org.joda" % "joda-convert" % "2.2.1"
libraryDependencies += "org.apache.xmlgraphics" % "batik-transcoder" % batikVersion
libraryDependencies += "com.google.guava" % "guava" % "23.0"
// deprecated from Java 9, needs to be added when
libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"
libraryDependencies += "org.specs2" %% "specs2-core" % specs2Version % "test"
libraryDependencies += "org.specs2" %% "specs2-scalacheck" % specs2Version % "test"
libraryDependencies += "org.specs2" %% "specs2-mock" % specs2Version % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit"  % akkaVersion % "test"
libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"

