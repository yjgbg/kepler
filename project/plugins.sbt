addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.13.2")
// 检测是否有落后的版本
// dependencyUpdates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.2.0")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.2.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.0.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.3.7")
addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.4")