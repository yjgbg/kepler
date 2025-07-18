def proj(projectName:String) = Project(projectName,file(projectName))
  .settings(
    name := projectName,
    organization := "com.yjgbg",
    scalaVersion := "3.7.1",
    version := "1.0.8",
    scalacOptions += "-Yexplicit-nulls",
    target := file(s"target/projects/${projectName}"),
    Compile / unmanagedSourceDirectories := Seq(baseDirectory.value / "src"),
    Compile / unmanagedResourceDirectories := Seq(baseDirectory.value / "src"),
    Test / unmanagedSourceDirectories := Seq(baseDirectory.value / "src-test"),
    Test / unmanagedResourceDirectories := Seq(baseDirectory.value / "src-test"),
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1",
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.7.0",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,
  )
def lib(name:String) = proj(s"lib-$name")
  .settings(
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
    )
  )
def appJvm(name:String) = proj(s"app-jvm-$name")
  .settings(
    assembly / assemblyJarName := s"${name}.jar",
  )
def appJs(name:String) = proj(s"app-js-$name")
  .settings(
  )
def appNative(name:String) = proj(s"app-native-$name")
  .settings(
  )
lazy val kepler = lib("kepler")
  .settings(
    libraryDependencies += "org.yaml" % "snakeyaml" % "2.4",
    libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.9.0"
  )


  // helm upgrade --install --namespace kube-system nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
  //   --set nfs.server=192.168.31.200 \
  //   --set nfs.path=/media/nfs
