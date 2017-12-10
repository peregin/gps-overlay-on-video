resolvers += "staged dependency-graph" at "https://oss.sonatype.org/content/repositories/netvirtual-void-1001"

resolvers += "oss snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// eclipse support
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.3")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.7.0")

// another installer plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
