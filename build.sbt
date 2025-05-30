import ProjectInfo._
import kevinlee.sbt.SbtCommon.crossVersionProps
import just.semver.SemVer
import SemVer.{Major, Minor}

ThisBuild / scalaVersion       := props.ProjectScalaVersion
ThisBuild / organization       := "io.kevinlee"
ThisBuild / organizationName   := "Kevin's Code"
ThisBuild / crossScalaVersions := props.CrossScalaVersions

ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  )
)
ThisBuild / homepage   := url(s"https://github.com/${props.GitHubUsername}/${props.TheProjectName}").some
ThisBuild / scmInfo    := ScmInfo(
  url(s"https://github.com/${props.GitHubUsername}/${props.TheProjectName}"),
  s"git@github.com:${props.GitHubUsername}/${props.TheProjectName}.git",
).some
ThisBuild / licenses   := List("MIT" -> url("http://opensource.org/licenses/MIT"))

ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"

lazy val justSysprocess = projectCommonSettings("justSysprocess", ProjectName(""), file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin)
  .settings(
    description               := "Sys Process Util",
    Compile / unmanagedSourceDirectories ++= {
      val sharedSourceDir = baseDirectory.value / "src/main"
      if (scalaVersion.value.startsWith("2.13") || scalaVersion.value.startsWith("2.12"))
        Seq(sharedSourceDir / "scala-2.12_2.13")
      else
        Seq.empty
    },
    libraryDependencies       :=
      crossVersionProps(
        List.empty,
        SemVer.parseUnsafe(scalaVersion.value),
      ) {
        case (Major(2), Minor(10), _) =>
          libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover")
        case x =>
          libraryDependencies.value
      },
    libraryDependencies       := (if (scalaVersion.value.startsWith("3.")) {
                              libraryDependencies
                                .value
                                .filterNot(props.removeDottyIncompatible)
                            } else {
                              libraryDependencies.value
                            }),
    console / initialCommands :=
      """import just.sysprocess._""",
  )
  .settings(mavenCentralPublishSettings)

lazy val props =
  new {
    val DottyVersion        = "3.0.2"
    val ProjectScalaVersion = DottyVersion

    val SonatypeCredentialHost = "s01.oss.sonatype.org"
    val SonatypeRepository     = s"https://$SonatypeCredentialHost/service/local"

    val removeDottyIncompatible: ModuleID => Boolean =
      m =>
        m.name == "wartremover" ||
          m.name == "ammonite" ||
          m.name == "kind-projector" ||
          m.name == "better-monadic-for" ||
          m.name == "mdoc"

    val CrossScalaVersions =
      List(
        "2.11.12",
        "2.12.13",
        "2.13.5",
        ProjectScalaVersion,
      ).distinct

    val IncludeTest = "compile->compile;test->test"

    val hedgehogVersion = "0.9.0"

    private val gitHubRepo = findRepoOrgAndName

    val GitHubUsername = gitHubRepo.fold("Kevin-Lee")(_.orgToString)
    val TheProjectName = gitHubRepo.fold("just-sysprocess")(_.nameToString)
  }

lazy val libs =
  new {

    lazy val hedgehog: List[ModuleID] = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

  }

lazy val mavenCentralPublishSettings: SettingsDefinition = List(
  /* Publish to Maven Central { */
  sonatypeCredentialHost := props.SonatypeCredentialHost,
  sonatypeRepository     := props.SonatypeRepository,
  /* } Publish to Maven Central */
)

def prefixedProjectName(name: String) = s"${props.TheProjectName}${if (name.isEmpty) "" else s"-$name"}"

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name                                    := prefixedProjectName(projectName.projectName),
      libraryDependencies ++= libs.hedgehog,
      /* WartRemover and scalacOptions { */
//      (Compile, compile) / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value),
//      (Test, compile) / wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value),
      wartremoverErrors ++= commonWarts((update / scalaBinaryVersion).value),
//      wartremoverErrors ++= Warts.all,
      Compile / console / wartremoverErrors   := List.empty,
      Compile / console / wartremoverWarnings := List.empty,
      Compile / console / scalacOptions       :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / wartremoverErrors      := List.empty,
      Test / console / wartremoverWarnings    := List.empty,
      Test / console / scalacOptions          :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      /* } WartRemover and scalacOptions */
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
      /* Ammonite-REPL { */
      libraryDependencies ++=
        (scalaBinaryVersion.value match {
          case "2.13" =>
            List("com.lihaoyi" % "ammonite" % "2.4.0-23-76673f7f" % Test cross CrossVersion.full)
          case "2.12" =>
            List("com.lihaoyi" % "ammonite" % "2.4.0-23-76673f7f" % Test cross CrossVersion.full)
          case "2.11" =>
            List("com.lihaoyi" % "ammonite" % "1.6.7" % Test cross CrossVersion.full)
          case _ =>
            List.empty[ModuleID]
        }),
      Test / sourceGenerators +=
        (scalaBinaryVersion.value match {
          case "2.13" | "2.11" | "2.12" =>
            task {
              val file = (Test / sourceManaged).value / "amm.scala"
              IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
              Seq(file)
            }
          case _ =>
            task(Seq.empty[File])
        }),
      /* } Ammonite-REPL */
      licenses                                := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
    .settings(mavenCentralPublishSettings)
