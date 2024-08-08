import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease._
import ReleaseStateTransformations._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

organization := "com.github.peregin"
name := "telemetry-on-video"

val entryPoint = "peregin.gpv.GpsOverlayApp"

Compile / mainClass := Some(entryPoint)

scalaVersion := "2.13.14"

// suppress warnings for unused settings introduced by plugins (e.g. github)
Global / excludeLintKeys ++= Set(ghreleaseNotes)

val jacksonVersion = "2.17.2"
val json4sVersion = "4.0.7"
val akkaVersion = "2.8.6"
val specs2Version = "4.20.8"
val logbackVersion = "1.5.6"
val batikVersion = "1.17" // svg manipulation
val xmlVersion = "2.3.0"
val jodaVersion = "2.12.7"
val swingVersion = "3.0.0"
val javacvVersion = "1.5.10"
val geotoolsVersion = "31.3"

scalacOptions ++= List("-feature", "-deprecation", "-language:implicitConversions", "-language:reflectiveCalls")
val macDockNameOpt = "-Xdock:name=\"GPS Overlay\""

run / fork := true

val moreJavaOptions = Seq(
  //macDockNameOpt, // supported on MacOs only
  "--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/java.net=ALL-UNNAMED"
)
javaOptions ++= moreJavaOptions
javacOptions ++= Seq("-source", "17", "-target", "17")

transitiveClassifiers in Global := Seq(Artifact.SourceClassifier)
resolvers ++= Seq(
  "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
  "GeoTools Repository" at "https://repo.osgeo.org/repository/release/",
)

assembly / mainClass := Some(entryPoint)
assembly / assemblyJarName := "gps-overlay-on-video.jar"
assembly / assemblyOption := (assembly / assemblyOption).value
  .withPrependShellScript(prependShellScript = Some(defaultUniversalScript(javaOpts = moreJavaOptions, shebang = false)))
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", ps @ _*) if ps.last.endsWith(".SF") || ps.last.endsWith(".RSA") || ps.last.endsWith(".DES") => MergeStrategy.discard
  case PathList("META-INF", "services", _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF", "maven", _*) => MergeStrategy.discard
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", _*) => MergeStrategy.first
  case PathList("javax", "servlet", _*) => MergeStrategy.discard
  case PathList("junit", _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
(assembly / test) := {}
Compile / assembly / artifact := {
  val art = (Compile / assembly / artifact).value
  art.withClassifier(Some("assembly"))
}
addArtifact(Compile / assembly / artifact, assembly)

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
    ),
    // because of: org.swinglabs:swingx-core           : 1.6.2-2 -> 1.6.2
    dependencyUpdatesFilter -= moduleFilter(organization = "org.swinglabs", name = "swingx-core"),
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


libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % swingVersion
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % xmlVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "org.swinglabs" % "swingx-core" % "1.6.2-2"
libraryDependencies += "org.swinglabs" % "swingx-ws" % "1.0"
libraryDependencies += "com.jgoodies" % "looks" % "2.2.2"
libraryDependencies += "com.jgoodies" % "jgoodies-common" % "1.8.1"
libraryDependencies += "com.miglayout" % "miglayout" % "3.7.4"
libraryDependencies += "org.bytedeco" % "javacv" % javacvVersion
libraryDependencies += "org.bytedeco" % "javacv-platform" % javacvVersion
libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
libraryDependencies += "org.json4s" %% "json4s-native" % json4sVersion
libraryDependencies += "org.json4s" %% "json4s-jackson" % json4sVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion
libraryDependencies += "ch.qos.logback" % "logback-core" % logbackVersion
libraryDependencies += "joda-time" % "joda-time" % jodaVersion
libraryDependencies += "org.joda" % "joda-convert" % "2.2.3"
libraryDependencies += "org.apache.xmlgraphics" % "batik-transcoder" % batikVersion
libraryDependencies += "com.google.guava" % "guava" % "33.2.1-jre"
libraryDependencies += "org.geotools" % "gt-referencing" % geotoolsVersion exclude("javax.media", "jai_core")
// deprecated from Java 9, needs to be added when
libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"
libraryDependencies += "org.specs2" %% "specs2-core" % specs2Version % "test"
libraryDependencies += "org.specs2" %% "specs2-scalacheck" % specs2Version % "test"
libraryDependencies += "org.specs2" %% "specs2-mock" % specs2Version % "test"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit"  % akkaVersion % "test"
libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % "test"

