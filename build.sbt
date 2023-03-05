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
lazy val versionCirce = "0.14.1"
lazy val versionJackson = "2.14.2"
lazy val versionScribe = "3.11.1"
lazy val versionZio = "2.0.9"
lazy val versionZhttp = "2.0.0-RC7"
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
    libraryDependencies += "io.d11" %% "zhttp" % versionZhttp,
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % versionJackson,
    libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % versionJackson,
    libraryDependencies += "com.outr" %% "scribe" % versionScribe,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-test" % versionZio % Test,
      "dev.zio" %% "zio-test-sbt" % versionZio % Test,
      "dev.zio" %% "zio-test-magnolia" % versionZio % Test,
      "io.d11" %% "zhttp-test" % versionZhttp % Test
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
lazy val keplerJsonDsl = (project in file("./json-dsl"))
  .settings(
    name := "kepler-json-dsl",
    version := "1.0.0-SNAPSHOT",
    organization := "com.github.yjgbg",
    libraryDependencies += "io.circe" %% "circe-core" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-generic" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-parser" % versionCirce,
    libraryDependencies += "io.circe" %% "circe-yaml" % versionCirce,
    scalacOptions += "-source:future", // 为了better-monadic-for
    scalacOptions += "-Yexplicit-nulls", //  因为protoc编译出的代码不支持explicit null，会导致编译失败，因此注释掉这行
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