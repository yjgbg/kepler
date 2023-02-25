lazy val core = (project in file("./core"))
  .settings(
    name := "core",
    scalaVersion := "3.3.0-RC3",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0",
    libraryDependencies += "io.d11" %% "zhttp"  % "2.0.0-RC7",
    libraryDependencies += "io.d11" %% "zhttp-test" % "2.0.0-RC7" % Test,
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "protobuf"
    ),
    // scalacOptions += "-Yexplicit-nulls",
    assemblyMergeStrategy := { // 打包时解决文件冲突的策略
      case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
      case x =>
        val oldStrategy = assemblyMergeStrategy.value
        oldStrategy(x)
    },
  )