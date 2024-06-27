// 检测是否有落后的版本
// dependencyUpdates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")
addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.16.0")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.4.17")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.2.0")