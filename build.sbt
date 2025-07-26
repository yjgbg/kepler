ThisBuild / semanticdbEnabled := true
def project(name:String):Project = Project(name,file(name)).settings(
  organization := "com.yjgbg",
  scalaVersion := "3.7.1",
  version := "1.0.16",
  versionScheme := Some("early-semver"),
  scalacOptions += "-Yexplicit-nulls",
  Compile / unmanagedSourceDirectories := Seq(baseDirectory.value / "src"),
  Compile / unmanagedResourceDirectories := (Compile / unmanagedSourceDirectories).value,
  Test / unmanagedSourceDirectories := Seq(baseDirectory.value / "src-test"),
  Test / unmanagedResourceDirectories := (Test / unmanagedSourceDirectories).value,
  libraryDependencies += "com.outr" %%% "scribe" % "3.17.0",
  libraryDependencies += "org.scala-lang" %%% "toolkit" % "0.7.0",
  libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.18.1" % Test,
)
def lib(name:String,enableScalaJsPlugin:Boolean = true) = project(s"lib-$name")
  .configure(it => if (enableScalaJsPlugin) it.enablePlugins(ScalaJSPlugin) else it)
  .settings(
    moduleName := name,
    publishTo := Some("GitHub Package Registry" at "https://maven.pkg.github.com/yjgbg/kepler"),
    publishMavenStyle := true,
    scmInfo := Some(ScmInfo(
      url("https://github.com/yjgbg/kepler"),
      "scm:git:git@github.com:yjgbg/kepler.git"
    )),
    credentials += Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      "yjgbg",
      sys.env.getOrElse("GITHUB_TOKEN","")
    ),
  )
def appJvm(name:String):Project = project(s"app-jvm-$name").settings(assembly / assemblyJarName := s"${name}.jar")
def appJs(name:String):Project = project(s"app-js-$name").enablePlugins(ScalaJSPlugin)
  .settings(scalaJSUseMainModuleInitializer := true,scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) })
lazy val libDemo:Project = lib("demo")
lazy val jvmAppDemo = appJvm("demo").dependsOn(libDemo)
lazy val nodeJsAppDemo = appJs("node-demo").dependsOn(libDemo)
lazy val webAppDemo = appJs("web-demo").dependsOn(libDemo)
lazy val electronAppDemo = appJs("electron-demo").dependsOn(libDemo)
lazy val kepler:Project = lib("kepler",false)
  .settings(
    libraryDependencies += "org.yaml" % "snakeyaml" % "2.4",
    libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.9.0"
  )
  // helm upgrade --install --namespace kube-system nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  //   --set nfs.server=192.168.31.200 \
  //   --set nfs.path=/media/nfs
