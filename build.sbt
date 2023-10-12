ThisBuild / scalaVersion := "3.3.0"
ThisBuild / scalacOptions += "-Yexplicit-nulls"
ThisBuild / scalacOptions += "-Wunused:all"
ThisBuild / credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USERNAME", "SONATYPE_USERNAME"),
  sys.env.getOrElse("SONATYPE_PASSWORD", "SONATYPE_USERNAME")
)
lazy val versionCirce = "0.14.5"
lazy val versionCirceYaml = "0.14.2"
lazy val verisonSourcecode = "0.3.0"
lazy val keplerJsonDsl = (project in file("./kepler-json-dsl"))
  .settings(
    name := "kepler-json-dsl",
    version := "1.0.0-SNAPSHOT",
    organization := "com.github.yjgbg",
    libraryDependencies += "io.circe" %% "circe-core" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-generic" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-parser" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-yaml" % versionCirceYaml,
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % verisonSourcecode,
    scalacOptions += "-source:future", // 为了better-monadic-for
    scalacOptions += "-Yexplicit-nulls",
    publishMavenStyle := true,
    publishTo := Some {
      if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
      else Opts.resolver.sonatypeStaging
    },
    versionScheme := Some("early-semver"),
  )