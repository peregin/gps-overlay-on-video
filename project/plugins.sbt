resolvers += "staged dependency-graph" at "https://oss.sonatype.org/content/repositories/netvirtual-void-1001"

resolvers += "oss snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// because of Java 11
libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"

// it is sourced by sbt from 1.4.x
addDependencyTreePlugin

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")

// generate fat jar
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.1")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.3")

// Gerolf's release plugin
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

// github publishing
addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.7.0")

// generate bill of materials
addSbtPlugin("com.github.sbt" %% "sbt-sbom" % "0.5.0")
