// 默认版本过低，会导致在m1 mac上找不到对应的protoc的包。
// 后续版本升级之后，如果不再出现这个问题，可以考虑删掉这行
ThisBuild / PB.protocVersion := "3.22.0"
ThisBuild / scalaVersion := "3.2.2"
ThisBuild / credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  sys.env.getOrElse("SONATYPE_USERNAME", "SONATYPE_USERNAME"),
  sys.env.getOrElse("SONATYPE_PASSWORD", "SONATYPE_USERNAME")
)
lazy val protobuf = (project in file("./protobuf"))
  .settings(
    name := "protobuf",
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "protobuf"
    )
  )
lazy val core = (project in file("./core"))
  .dependsOn(protobuf)
  .settings(
    name := "core",
    libraryDependencies += "io.d11" %% "zhttp" % "2.0.0-RC7",
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.2",
    libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.14.2",
    libraryDependencies += "com.outr" %% "scribe" % "3.11.1",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-test" % "2.0.9" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.9" % Test,
      "dev.zio" %% "zio-test-magnolia" % "2.0.9" % Test,
      "io.d11" %% "zhttp-test" % "2.0.0-RC7" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions += "-source:future", // 为了better-monadic-for
    scalacOptions += "-Yexplicit-nulls", //  因为protoc编译出的代码不支持explicit null，会导致编译失败，因此注释掉这行
    // scalacOptions += "-Xfatal-warnings", // 严格的编译，遇到warning会导致编译错误
    assemblyMergeStrategy := { // 打包时解决文件冲突的策略
      case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.first
      case PathList(ps @ _*) if ps.last endsWith ("module-info.class")          => MergeStrategy.last
      case x                                                                    => assemblyMergeStrategy.value(x)
    },
    assemblyJarName := "app.jar",
    outputStrategy := Some(StdoutOutput) // sbt中打日志的配置
  )
lazy val circeVersion = "0.14.1"
lazy val keplerJsonDsl = (project in file("./json-dsl"))
  .settings(
    name := "kepler-json-dsl",
    version := "1.0.0-SNAPSHOT",
    organization := "com.github.yjgbg",
    libraryDependencies += "io.circe" %% "circe-core" % circeVersion,
    libraryDependencies += "io.circe" %% "circe-generic" % circeVersion,
    libraryDependencies += "io.circe" %% "circe-parser" % circeVersion,
    libraryDependencies += "io.circe" %% "circe-yaml" % circeVersion,
    publishMavenStyle := true,
    publishTo := Some {
      if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
      else Opts.resolver.sonatypeStaging
    },
    versionScheme := Some("early-semver")
  )
lazy val devops = (project in file("./devops"))
  .dependsOn(keplerJsonDsl)
  .settings(
    name := "devops"
  )