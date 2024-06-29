import org.scalacheck.Properties
import org.scalacheck.Prop.*
import com.github.yjgbg.kepler.dsl.ForTest
object Test0 extends Properties("String"):
  property("length") = forAll:(s0:String,s1:String) =>
    (s0 + s1).length() == (s1 + s0).length() 
  property("foldLeft == foldRight") = forAll:(list:Seq[String]) => 
    val x = list.foldLeft(""){_ + _}
    val y = list.foldRight(""){_ + _}
    x == y
  property("ForTest") = forAll: (a:String,b:String) => 
    ForTest.f(a,b) == ForTest.f(b,a)
  property("xxx") = forAll:(a:String,b:String) =>
    import com.github.yjgbg.kepler.dsl.kubernetes
    import kubernetes.base.{*,given}
    context("orbstack"):
      namespace("default"):
          import kubernetes.{name => _name,*,given}
          PersistentVolumeClaim:
            metadata(_name := "123")
            spec(storageClassName := "storageClassName",accessModes += "ReadWriteOnce")
          ConfigMap:
            metadata(_name := "")
            data := Map(
             "application.yml" -> raw"""
               |spring:
               |  datasource:
               |    url: qwejqwiehkwehdqwliejo
               |""".stripMargin.stripTrailing().nn.stripLeading().nn
            )
          Pod:
            metadata(_name := "123",_labels("1" -> "2","3" -> "4"))
            spec:
              containers(_name := "",image := "")
          CronJob:
            metadata(_name := "123")
            spec(suspend := false):
              failedJobsHistoryLimit := 3
              successfulJobsHistoryLimit := 4
              jobTemplate:
                spec:
                  backoffLimit := 3
                  template:
                    metadata(labels := Map("app" -> "123"))
          Job:
            metadata(_name := "123")
            spec(backoffLimit := 3)
          Deployment:
            metadata(_name := "nginx")
            spec:
              selector += "app" -> "nginx"
              template:
                metadata(labels := Map("app" -> "nginx"))
                spec:
                  hostAliases += Seq("www.baidu.com") -> "192.168.50.1"
                  volumeEmptyDir += "volumeName0"
                  volumeConfigMap += "volumeName1" -> "config-map-name"
                  volumePVC += "volumeName2" -> "pvc-name"
                  initContainers:
                    livenessProbe:
                      initialDelaySeconds := 3
                      action := Action.Exec("curl -x 'localhost:8080/api/healthy'")
                    env += "spring.profiles.active" -> "prod"
                    ports += 80
                  containers(_name := "aaa"):
                    image := "bbb"
    true