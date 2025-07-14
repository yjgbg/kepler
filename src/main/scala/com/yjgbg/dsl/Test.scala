package com.github.yjgbg.kepler.dsl

object ForTest {
  @main def run =
    import GitlabCi.{*,given}
    GitlabCi:
      stage := Seq("compile","test","bootJar")
      job("compile"):
        stage := "compile"
        image := "gradle:9.0"
        script += "gradle classes"
        script += "gradle classes"
        script += "gradle classes"
      job("test"):
        stage := "test"
        needs += "compile"
        image := "openjdk:21"
        script += "gradle test"
      job("bootJar"):
        stage := "bootJar"
        needs += "compile"
        image := "openjdk:21"
        script += "gradle bootJar"
}
