ThisBuild / PB.protocVersion := "3.22.0" 
// 默认版本过低，会导致在m1 mac上找不到对应的protoc的包。
// 后续版本升级之后，如果不再出现这个问题，可以考虑删掉这行
lazy val core = (project in file("./core"))
  .settings(
    name := "core",
    scalaVersion := "3.2.2",
    libraryDependencies += "io.d11" %% "zhttp"  % "2.0.0-RC7",
    libraryDependencies += "com.outr" %% "scribe" % "3.11.1", // 日志，接入slf4j
    libraryDependencies += "io.d11" %% "zhttp-test" % "2.0.0-RC7" % Test,
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "protobuf"
    ),
    scalacOptions += "-source:future", // 为了better-monadic-for
    // scalacOptions += "-Yexplicit-nulls",//  因为protoc编译出的代码不支持explicit null，会导致编译失败，因此注释掉这行
    // scalacOptions += "-Xfatal-warnings", // 严格的编译，遇到warning会导致编译错误
    assemblyMergeStrategy := { // 打包时解决文件冲突的策略
      case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
      case x => assemblyMergeStrategy.value(x)
    },
    assemblyJarName := "app.jar",
    outputStrategy := Some(StdoutOutput) // sbt中打日志的配置
  )