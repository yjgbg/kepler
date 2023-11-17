ThisBuild / scalaVersion := "3.3.0"
ThisBuild / scalacOptions ++= Seq(
  "-Yexplicit-nulls","-Wunused:all","-source:future"
)
ThisBuild / scalacOptions += "-Wunused:all"
ThisBuild / credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USERNAME", "SONATYPE_USERNAME"),
  sys.env.getOrElse("SONATYPE_PASSWORD", "SONATYPE_USERNAME")
)
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / organization := "com.github.yjgbg" 
lazy val versionCirce = "0.14.5"
lazy val versionCirceYaml = "0.14.2"
lazy val verisonSourcecode = "0.3.0"
lazy val `kepler-json-dsl` = (project in file("./kepler-json-dsl"))
  .settings(
    name := "kepler-json-dsl",
    libraryDependencies += "io.circe" %% "circe-core" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-generic" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-parser" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-yaml" % versionCirceYaml,
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % verisonSourcecode,
    publishMavenStyle := true,
    publishTo := Some {
      if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
      else Opts.resolver.sonatypeStaging
    },
    versionScheme := Some("early-semver"),
  )

lazy val `kepler-dsl` = (project in file ("kepler-dsl"))
    .settings(
      name := "kepler-dsl-std",
      idePackagePrefix := Some(organization.value + ".kepler.dsl"),
    )