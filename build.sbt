ThisBuild / organization := "com.yjgbg"
ThisBuild / scalaVersion := "3.7.1"
ThisBuild / scalacOptions += "-Yexplicit-nulls"
ThisBuild / version := "1.0.7"
lazy val kepler = (project in file("."))
  .settings(
    // target := {
    //   val path = file(".").toPath()
    //     .toAbsolutePath()
    //     .relativize(baseDirectory.value.toPath())
    //     .toString()
    //   file(s"target/projects/${path}")
    // },
    // Compile / unmanagedSourceDirectories ~= 
    //   {_.map{_.getParentFile.getParentFile}.distinct},
    // Compile / unmanagedResourceDirectories ~= 
    //   {_.map{_.getParentFile.getParentFile}.distinct},
    // Test / unmanagedSourceDirectories ~= 
    //   {_.map{_.getParentFile.getParentFile.getParentFile / "src-test"}.distinct},
    // Test / unmanagedResourceDirectories ~= 
    //   {_.map{_.getParentFile.getParentFile.getParentFile / "src-test"}.distinct},
    // libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.18.0" % Test
  )
  .settings(
    name := "kepler",
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1",
    libraryDependencies += "org.yaml" % "snakeyaml" % "2.4",
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.7.0"
  )
  .settings(
    publishTo := Some("GitHub Package Registry" at "https://maven.pkg.github.com/yjgbg/kepler"),
    publishMavenStyle := true,
    scmInfo := Some(ScmInfo(
      url("https://github.com/yjgbg/kepler"),
      "scm:git:git@github.com:yjgbg/kepler.git"
    )),
    // credentials += Credentials(
    //   "GitHub Package Registry",
    //   "maven.pkg.github.com",
    //   "yjgbg",
    //   sys.env.getOrElse("GITHUB_TOKEN","")
    // )
  )
