
// gen-idea plugin for IntelliJ
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// eclipse support
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

// to generate dependency graph of the libraries
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

// check latest updates form maven
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.5")

// xml data binding
addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.1.2")

// generates build information, timestamp
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.1")
