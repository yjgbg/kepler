
lazy val api = (project in file("./core"))
  .settings(
    name := "api",
    scalaVersion := "3.2.2",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0",
    libraryDependencies += "io.d11" %% "zhttp"  % "2.0.0-RC7",
    libraryDependencies += "io.d11" %% "zhttp-test" % "2.0.0-RC7" % Test,
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
    )
  )