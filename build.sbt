ThisBuild / scalaVersion := "3.3.3"
ThisBuild / scalacOptions ++= Seq(
  "-Yexplicit-nulls","-Wunused:all","-source:3.3"
)
def std(project:Project):Project = project
  .settings(
    target := {
      val path = file(".").toPath()
      .toAbsolutePath()
      .relativize(baseDirectory.value.toPath())
      .toString()
      file(s"target/projects/${path}")
    },
    Compile / unmanagedSourceDirectories ~= 
      {_.map{_.getParentFile.getParentFile.getParentFile}.distinct},
    Compile / unmanagedResourceDirectories ~= 
      {_.map{_.getParentFile.getParentFile.getParentFile}.distinct},
  )
def graal(project:Project): Project = project
  .enablePlugins(NativeImagePlugin)
  .settings(
    nativeImageJvm := "graalvm-community",
    nativeImageVersion := "21.0.2",
    nativeImageJvmIndex := "cs"
  )
ThisBuild / credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USERNAME", "SONATYPE_USERNAME"),
  sys.env.getOrElse("SONATYPE_PASSWORD", "SONATYPE_USERNAME")
)
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / organization := "com.github.yjgbg" 

lazy val `kepler-dsl` = (project in file ("kepler-dsl"))
  .configure(std)
  .settings(
    name := "kepler-dsl-std",
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1"
  )
lazy val `kepler-dsl-ex` = (project in file ("kepler-dsl-ex"))
  .configure(std)
  .settings(
    name := "kepler-dsl-ex",
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1"
  )