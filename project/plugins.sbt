resolvers += "staged dependency-graph" at "https://oss.sonatype.org/content/repositories/netvirtual-void-1001"

resolvers += "oss snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// because of Java 11
libraryDependencies += "com.sun.activation" % "javax.activation" % "1.2.0"

// it is sourced by sbt from 1.4.x
addDependencyTreePlugin

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.11.0")

// generate fat jar
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.1")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.7")

// Gerolf's release plugin
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

// github publishing
addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.7.0")
