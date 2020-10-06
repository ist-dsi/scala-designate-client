organization := "pt.tecnico.dsi"
name := "scala-designate-client"

// ======================================================================================================================
// ==== Compile Options =================================================================================================
// ======================================================================================================================
javacOptions ++= Seq("-Xlint", "-encoding", "UTF-8", "-Dfile.encoding=utf-8")
scalaVersion := "2.13.3"

scalacOptions ++= Seq(
  "-encoding", "utf-8",            // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:higherKinds",         // Just to help Intellij, otherwise he keeps asking to import/enable the higherKinds flag
  "-Ybackend-parallelism", "8",    // Maximum worker threads for backend.
  "-Ybackend-worker-queue", "10",  // Backend threads worker queue size.
  "-Ymacro-annotations",           // Enable support for macro annotations, formerly in macro paradise.
  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
  "-Xsource:3",                    // Treat compiler input as Scala source for the specified version.
  "-Xmigration:3",                 // Warn about constructs whose behavior may have changed since version.
  "-Werror",                       // Fail the compilation if there are any warnings.
  "-Xlint:_",                      // Enables every warning. scalac -Xlint:help for a list and explanation
  "-deprecation",                  // Emit warning and location for usages of deprecated APIs.
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-Wdead-code",                   // Warn when dead code is identified.
  "-Wextra-implicit",              // Warn when more than one implicit parameter section is defined.
  "-Wnumeric-widen",               // Warn when numerics are widened.
  "-Woctal-literal",               // Warn on obsolete octal syntax.
  "-Wvalue-discard",               // Warn when non-Unit expression results are unused.
  "-Wunused:_",                    // Enables every warning of unused members/definitions/etc
)

scalacOptions in (Compile, console) ~= (_.filterNot { option =>
  option.startsWith("-W") || option.startsWith("-Xlint")
})
scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

// ======================================================================================================================
// ==== Dependencies ====================================================================================================
// ======================================================================================================================
libraryDependencies ++= Seq(
  "pt.tecnico.dsi"  %% "scala-keystone-client" % "0.5.0-SNAPSHOT",
  "com.beachape"    %% "enumeratum-circe"      % "1.6.1",
  "ch.qos.logback"  %  "logback-classic"       % "1.2.3" % Test,
  "org.scalatest"   %% "scalatest"             % "3.2.2" % Test,
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

resolvers += Resolver.sonatypeRepo("snapshots")

// ======================================================================================================================
// ==== Testing =========================================================================================================
// ======================================================================================================================
// http://www.scalatest.org/user_guide/using_the_runner
//   -o[configs...] - causes test results to be written to the standard output.
//      D - show all durations
//      F - show full stack traces
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")

// By default logging is buffered for each test source file until all tests for that file complete.
// This disables it, causing SBT testing logs to actually show they are running in parallel.
Test / logBuffered := false
// By default, tests executed in a forked JVM are executed sequentially.
Test / fork := true
// So we make them run in parallel.
Test / testForkedParallel := true
// We need to force IPv6 addresses otherwise Java resolves names to IPv4 and Travis will fail because the VM test is not reachable by IPv4
Test / javaOptions += "-Djava.net.preferIPv6Addresses=true"
// The tests in a single group are run sequentially. We run 4 suites per forked VM.
Test / testGrouping := {
  import sbt.Tests.{Group, SubProcess}
  (Test / definedTests).value.grouped(4).zipWithIndex.map { case (tests, index) =>
    Group(index.toString, tests, SubProcess(ForkOptions().withRunJVMOptions((Test / javaOptions).value.toVector)))
  }.toSeq
}

// ======================================================================================================================
// ==== Scaladoc ========================================================================================================
// ======================================================================================================================
git.remoteRepo := s"git@github.com:ist-dsi/${name.value}.git"
git.useGitDescribe := true // Get version by calling `git describe` on the repository
val latestReleasedVersion = SettingKey[String]("latest released version")
latestReleasedVersion := git.gitDescribedVersion.value.getOrElse("0.0.1-SNAPSHOT")

// Define the base URL for the Scaladocs for your library. This will enable clients of your library to automatically
// link against the API documentation using autoAPIMappings.
apiURL := Some(url(s"${homepage.value.get}/api/${latestReleasedVersion.value}/"))
autoAPIMappings := true // Tell scaladoc to look for API documentation of managed dependencies in their metadata.
scalacOptions in (Compile, doc) ++= Seq(
  "-author",      // Include authors.
  "-diagrams",    // Create inheritance diagrams for classes, traits and packages.
  "-groups",      // Group similar functions together (based on the @group annotation)
  "-implicits",   // Document members inherited by implicit conversions.
  "-doc-title", name.value.capitalize,
  "-doc-version", latestReleasedVersion.value,
  "-doc-source-url", s"${homepage.value.get}/tree/v${latestReleasedVersion.value}€{FILE_PATH}.scala",
  "-sourcepath", (baseDirectory in ThisBuild).value.getAbsolutePath,
)

enablePlugins(GhpagesPlugin, SiteScaladocPlugin)
siteSubdirName in SiteScaladoc := s"api/${version.value}"
excludeFilter in ghpagesCleanSite := AllPassFilter // We want to keep all the previous API versions
val latestFileName = "latest"
val createLatestSymlink = taskKey[Unit](s"Creates a symlink named $latestFileName which points to the latest version.")
createLatestSymlink := {
  import java.nio.file.Files
  // We use ghpagesSynchLocal instead of ghpagesRepository to ensure the files in the local filesystem already exist
  val path = (ghpagesSynchLocal.value / "api" / latestFileName).toPath
  if (!Files.isSymbolicLink(path)) Files.createSymbolicLink(path, new File(latestReleasedVersion.value).toPath)
}
ghpagesPushSite := ghpagesPushSite.dependsOn(createLatestSymlink).value
ghpagesNoJekyll := false
envVars in ghpagesPushSite := Map("SBT_GHPAGES_COMMIT_MESSAGE" -> s"Add Scaladocs for version ${latestReleasedVersion.value}")

// ======================================================================================================================
// ==== Publishing/Release ==============================================================================================
// ======================================================================================================================
publishTo := sonatypePublishTo.value
sonatypeProfileName := organization.value

licenses += "MIT" -> url("http://opensource.org/licenses/MIT")
homepage := Some(url(s"https://github.com/ist-dsi/${name.value}"))
scmInfo := Some(ScmInfo(homepage.value.get, git.remoteRepo.value))
developers ++= List(
  Developer("Lasering", "Simão Martins", "", url("https://github.com/Lasering")),
  Developer("afonsomatos", "Afonso Matos", "", url("https://github.com/afonsomatos")),
)

// Fail the build/release if updates there are updates for the dependencies
dependencyUpdatesFailBuild := true

releaseUseGlobalVersion := false
releaseNextCommitMessage := s"Setting version to ${ReleasePlugin.runtimeVersion.value} [skip ci]"

releasePublishArtifactsAction := PgpKeys.publishSigned.value // Maven Central requires packages to be signed
import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  releaseStepTask(dependencyUpdates),
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepTask(Compile / doc),
  releaseStepTask(Test / test),
  setReleaseVersion,
  tagRelease,
  releaseStepTask(ghpagesPushSite),
  publishArtifacts,
  releaseStepCommand("sonatypeRelease"),
  pushChanges,
  setNextVersion
)
