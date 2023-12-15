ThisBuild / scalaVersion := "3.3.1"
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

lazy val `kepler-dsl` = (project in file ("kepler-dsl"))
    .settings(
      name := "kepler-dsl-std",
      idePackagePrefix := Some(organization.value + ".kepler.dsl"),
      libraryDependencies += "ch.epfl.scala" % "bsp4j" % "2.2.0-M1"
    )