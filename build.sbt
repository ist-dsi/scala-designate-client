organization := "pt.tecnico.dsi"
name := "scala-designateclient"

// ======================================================================================================================
// ==== Compile Options =================================================================================================
// ======================================================================================================================
javacOptions ++= Seq("-Xlint", "-encoding", "UTF-8", "-Dfile.encoding=utf-8")
scalaVersion := "2.13.1"

scalacOptions ++= Seq(
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Explicitly enables the implicit conversions feature
  "-Ybackend-parallelism", "4",        // Maximum worker threads for backend.
  "-Ybackend-worker-queue", "10",      // Backend threads worker queue size.
  "-Ymacro-annotations",               // Enable support for macro annotations, formerly in macro paradise.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xmigration:2.14.0",                // Warn about constructs whose behavior may have changed since version.
  "-Xfatal-warnings", "-Werror",       // Fail the compilation if there are any warnings.
  //"-Xlint:_",                          // Enables every warning. scalac -Xlint:help for a list and explanation
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-Wdead-code",                       // Warn when dead code is identified.
  "-Wextra-implicit",                  // Warn when more than one implicit parameter section is defined.
  "-Wnumeric-widen",                   // Warn when numerics are widened.
  "-Woctal-literal",                   // Warn on obsolete octal syntax.
  //"-Wunused:_",                        // Enables every warning of unused members/definitions/etc
)
// These lines ensure that in sbt console or sbt test:console the -Ywarn* and the -Xfatal-warning are not bothersome.
// https://stackoverflow.com/questions/26940253/in-sbt-how-do-you-override-scalacoptions-for-console-in-all-configurations
scalacOptions in (Compile, console) ~= (_.filterNot { option =>
  option.startsWith("-Ywarn") || option == "-Xfatal-warnings" || option.startsWith("-W") || option.startsWith("-Xlint")
})
scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

// ======================================================================================================================
// ==== Dependencies ====================================================================================================
// ======================================================================================================================
libraryDependencies ++= Seq("blaze-client", "dsl", "circe").map { module =>
  "org.http4s"      %% s"http4s-$module" % "0.21.3"
} ++ Seq(
  "io.circe"        %% "circe-derivation"     % "0.13.0-M4",
  "io.circe"        %% "circe-parser"         % "0.13.0",
  "pt.tecnico.dsi"  %% "scala-keystoneclient" % "0.1.0-SNAPSHOT",
  "com.beachape"    %% "enumeratum-circe"     % "1.5.23",
  "ch.qos.logback"  % "logback-classic"      % "1.2.3" % Test,
  "org.scalatest"   %% "scalatest"            % "3.1.1" % Test,
)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

//coverageEnabled := true

// ======================================================================================================================
// ==== Testing =========================================================================================================
// ======================================================================================================================
// By default, logging is buffered for each test source file until all tests for that file complete. This disables it.
Test / logBuffered := false
// By default, tests executed in a forked JVM are executed sequentially.
Test / fork := true
// So we make them run in parallel.
Test / testForkedParallel := true
// The tests in a single group are run sequentially. We run 4 suites per forked VM.
Test / testGrouping := {
  import sbt.Tests.{Group, SubProcess}
  (Test / definedTests).value.grouped(4).zipWithIndex.map { case (tests, index) =>
    Group(index.toString, tests, SubProcess(ForkOptions()))
  }.toSeq
}

// http://www.scalatest.org/user_guide/using_the_runner
//   -o[configs...] - causes test results to be written to the standard output. The D configs shows all durations.
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

