import ProjectInfo._
import kevinlee.sbt.SbtCommon.crossVersionProps
import just.semver.SemVer
import SemVer.{Major, Minor}

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / organization := "io.kevinlee"
ThisBuild / organizationName := "Kevin's Code"
ThisBuild / crossScalaVersions := props.CrossScalaVersions

ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  )
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.TheProjectName}").some
ThisBuild / scmInfo := ScmInfo(
  url(s"https://github.com/${props.GitHubUsername}/${props.TheProjectName}"),
  s"git@github.com:${props.GitHubUsername}/${props.TheProjectName}.git",
).some
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))

lazy val justSysprocess = projectCommonSettings("justSysprocess", ProjectName(""), file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin)
  .settings(
    description := "Sys Process Util",
    Compile / unmanagedSourceDirectories ++= {
      val sharedSourceDir = baseDirectory.value / "src/main"
      if (scalaVersion.value.startsWith("2.13") || scalaVersion.value.startsWith("2.12"))
        Seq(sharedSourceDir / "scala-2.12_2.13")
      else
        Seq.empty
    },
    libraryDependencies :=
      crossVersionProps(
        List.empty,
        SemVer.parseUnsafe(scalaVersion.value),
      ) {
        case (Major(2), Minor(10), _) =>
          libraryDependencies.value.filterNot(m => m.organization == "org.wartremover" && m.name == "wartremover")
        case x                        =>
          libraryDependencies.value
      },
    libraryDependencies := (if (isDotty.value) {
                              libraryDependencies
                                .value
                                .filterNot(props.removeDottyIncompatible)
                            } else {
                              libraryDependencies.value
                            }),
    libraryDependencies := libraryDependencies.value.map(_.withDottyCompat(scalaVersion.value)),
    initialCommands in console :=
      """import just.sysprocess._""",
  )

lazy val props =
  new {
    val DottyVersion        = "3.0.0-RC2"
    val ProjectScalaVersion = DottyVersion

    val removeDottyIncompatible: ModuleID => Boolean =
      m =>
        m.name == "wartremover" ||
          m.name == "ammonite" ||
          m.name == "kind-projector" ||
          m.name == "better-monadic-for" ||
          m.name == "mdoc"

    val CrossScalaVersions: Seq[String] =
      Seq(
        "2.11.12",
        "2.12.13",
        "2.13.5",
        "3.0.0-M1",
        "3.0.0-M2",
        "3.0.0-M3",
        "3.0.0-RC1",
        ProjectScalaVersion,
      ).distinct

    lazy val scala3cLanguageOptions =
      "-language:" + List(
        "dynamics",
        "existentials",
        "higherKinds",
        "reflectiveCalls",
        "experimental.macros",
        "implicitConversions",
      ).mkString(",")

    val IncludeTest: String = "compile->compile;test->test"

    val hedgehogVersion = "0.6.5"

    val GitHubUsername = "Kevin-Lee"
    val TheProjectName = "just-sysprocess"
  }

lazy val libs =
  new {

    lazy val hedgehog: Seq[ModuleID] = Seq(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

  }

def prefixedProjectName(name: String) = s"${props.TheProjectName}${if (name.isEmpty)
  ""
else
  s"-$name"}"

def projectCommonSettings(id: String, projectName: ProjectName, file: File): Project =
  Project(id, file)
    .settings(
      name := prefixedProjectName(projectName.projectName),
      addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.11.3" cross CrossVersion.full),
      addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1"),
      useAggressiveScalacOptions := true,
      libraryDependencies ++= libs.hedgehog,
      scalacOptions := (isDotty.value match {
        case true  =>
          Seq(
            "-source:3.0-migration",
            props.scala3cLanguageOptions,
            "-Ykind-projector",
          )
        case false =>
          scalacOptions.value
      }),
      Compile / doc / scalacOptions := ((Compile / doc / scalacOptions)
        .value
        .filterNot(
          if (isDotty.value) {
            Set(
              "-source:3.0-migration",
              "-scalajs",
              "-deprecation",
              "-explain-types",
              "-explain",
              "-feature",
              props.scala3cLanguageOptions,
              "-unchecked",
              "-Xfatal-warnings",
              "-Ykind-projector",
              "-from-tasty",
              "-encoding",
              "utf8",
            )
          } else {
            Set.empty[String]
          }
        )),
      /* WartRemover and scalacOptions { */
//      wartremoverErrors in (Compile, compile) ++= commonWarts((scalaBinaryVersion in update).value),
//      wartremoverErrors in (Test, compile) ++= commonWarts((scalaBinaryVersion in update).value),
      wartremoverErrors ++= commonWarts((scalaBinaryVersion in update).value),
//      wartremoverErrors ++= Warts.all,
      Compile / console / wartremoverErrors := List.empty,
      Compile / console / wartremoverWarnings := List.empty,
      Compile / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      Test / console / wartremoverErrors := List.empty,
      Test / console / wartremoverWarnings := List.empty,
      Test / console / scalacOptions :=
        (console / scalacOptions)
          .value
          .filterNot(option => option.contains("wartremover") || option.contains("import")),
      /* } WartRemover and scalacOptions */
      testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
      /* Ammonite-REPL { */
      libraryDependencies ++=
        (scalaBinaryVersion.value match {
          case "2.13" =>
            Seq("com.lihaoyi" % "ammonite" % "2.3.8-58-aa8b2ab1" % Test cross CrossVersion.full)
          case "2.12" =>
            Seq("com.lihaoyi" % "ammonite" % "2.3.8-58-aa8b2ab1" % Test cross CrossVersion.full)
          case "2.11" =>
            Seq("com.lihaoyi" % "ammonite" % "1.6.7" % Test cross CrossVersion.full)
          case _      =>
            Seq.empty[ModuleID]
        }),
      sourceGenerators in Test +=
        (scalaBinaryVersion.value match {
          case "2.13"          =>
            task(Seq.empty[File])
          case "2.11" | "2.12" =>
            task {
              val file = (sourceManaged in Test).value / "amm.scala"
              IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
              Seq(file)
            }
          case _               =>
            task(Seq.empty[File])
        }),
      /* } Ammonite-REPL */
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
