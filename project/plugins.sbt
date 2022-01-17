logLevel := sbt.Level.Warn

addSbtPlugin("com.geirsson"    % "sbt-ci-release"  % "1.5.7")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.13")
addSbtPlugin("io.kevinlee"     % "sbt-docusaur"    % "0.8.1")

val sbtDevOopsVersion = "2.15.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
