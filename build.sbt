def std(project:Project):Project = project.settings(
  target := {
    val path = file(".").toPath()
      .toAbsolutePath()
      .relativize(baseDirectory.value.toPath())
      .toString()
    file(s"target/projects/${path}")
  },
  Compile / unmanagedSourceDirectories ~= 
    {_.map{_.getParentFile.getParentFile}.distinct},
  Compile / unmanagedResourceDirectories ~= 
    {_.map{_.getParentFile.getParentFile}.distinct},
  Test / unmanagedSourceDirectories ~= 
    {_.map{_.getParentFile.getParentFile.getParentFile / "src-test"}.distinct},
  Test / unmanagedResourceDirectories ~= 
    {_.map{_.getParentFile.getParentFile.getParentFile / "src-test"}.distinct},
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.18.0" % Test
  )
def graal(project:Project): Project = project.enablePlugins(NativeImagePlugin)
  .settings(
    nativeImageJvm := "graalvm-community",
    nativeImageVersion := "21.0.2",
    nativeImageJvmIndex := "cs"
  )
ThisBuild / organization := "com.github.yjgbg"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / scalacOptions += "-Yexplicit-nulls"
// ThisBuild / scalacOptions += "-Wunused:all"
ThisBuild / scalacOptions += "-source:3.3"
ThisBuild / version := "1.0.0-SNAPSHOT"
lazy val `kepler-dsl` = (project in file ("kepler-dsl"))
  .configure(std)
  .settings(
    name := "kepler-dsl",
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1"
  )
lazy val `kepler-dsl-docs` = (project in file ("kepler-dsl-docs"))
  .configure(std)
  .dependsOn(`kepler-dsl`)
  .enablePlugins(MdocPlugin)