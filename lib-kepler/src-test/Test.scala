import org.scalacheck.Properties
import org.scalacheck.Prop.forAll


object ForTest  extends Properties("GitlabCiTest"):
  property("main") = forAll:(s0:String,s1:String,s2:String) =>
    import kepler.GitlabCi.{*,given}
    GitlabCi:
      stage := Seq(s0,s1,s2)
      job(s0):
        stage := s0
        image := "gradle:9.0"
        script += "gradle classes"
        script += "gradle classes"
        script += "gradle classes"
      job(s1):
        stage := s1
        needs += "compile"
        image := "openjdk:21"
        script += "gradle test"
      job(s2):
        stage := s2
        needs += "compile"
        image := "openjdk:21"
        script += "gradle bootJar"
        cache:
          paths += "build"
          key := "build-cache"
          untracked := true
        artifacts:
          paths += "build/libs"
          expireIn := 60 * 60 * 24 * 30 // 7 days
    true
