resolvers += "staged dependency-graph" at "https://oss.sonatype.org/content/repositories/netvirtual-void-1001"

resolvers += "oss snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// gen-idea plugin for IntelliJ
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// eclipse support
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.9")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.1")

// static code analysis
addSbtPlugin("de.johoop" % "cpd4sbt" % "1.1.5")

// another installer plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0-RC1")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
