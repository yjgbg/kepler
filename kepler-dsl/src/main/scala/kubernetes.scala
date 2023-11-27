package com.github.yjgbg.kepler.dsl

object kubernetes:
  export core.*
  // 声明Scope和Key字面量
  val Namespace:"Namespace" = compiletime.constValue
  given MultiNodeKey[Namespace.type,Scope.Root,Namespace.type] = Key.multiNodeKey
  val Deployment:"Deployment" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[Deployment.type,A,Deployment.type] = Key.multiNodeKey
  val Service:"Service" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[Service.type,A,Service.type] = Key.multiNodeKey
  val ConfigMap: "ConfigMap" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[ConfigMap.type,A,ConfigMap.type] = Key.multiNodeKey
  val Secret:"Secret" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[Secret.type,A,Secret.type] = Key.multiNodeKey
  val Pod:"Pod" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[Pod.type,A,Pod.type] = Key.multiNodeKey
  val Job:"Job" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[Job.type,A,Job.type] = Key.multiNodeKey
  val CronJob: "CronJob" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[CronJob.type,A,CronJob.type] = Key.multiNodeKey
  val PersistentVolumeClaim: "PersistentVolumeClaim" = compiletime.constValue
  given [A <: _ >> Namespace.type]:MultiNodeKey[PersistentVolumeClaim.type,A,PersistentVolumeClaim.type] = Key.multiNodeKey
  type ResourceScope = _ >> Deployment.type
    | _ >> Service.type
    | _ >> ConfigMap.type
    | _ >> Secret.type
    | _ >> Pod.type
    | _ >> Job.type
    | _ >> CronJob.type
    | _ >> PersistentVolumeClaim.type
  val name:"name" = compiletime.constValue
  given [A<: _ >> Namespace.type | ResourceScope | _ >> containers.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  val labels:"labels" = compiletime.constValue
  given [A<: ResourceScope]:MultiValueKey[labels.type,A,(String,String)] = Key.multiValueKey
  val annotations:"annotations" = compiletime.constValue
  given [A<: ResourceScope]:MultiValueKey[annotations.type,A,(String,String)] = Key.multiValueKey
  val spec:"spec" = compiletime.constValue
  given [A<:ResourceScope]:SingleNodeKey[spec.type,A,spec.type] = Key.singleNodeKey
  val selector:"selector" = compiletime.constValue
  given [A<:
    _ >> Deployment.type >> spec.type
    | _ >> Service.type >> spec.type
  ]:MultiValueKey[selector.type,A,(String,String)] = Key.multiValueKey
  val template:"template" = compiletime.constValue
  given [A <: 
    _ >> Deployment.type >> spec.type
    | _ >> Job.type >> spec.type
    ]:SingleNodeKey[template.type,A,Pod.type] = Key.singleNodeKey
  val hostAliases:"hostAliases" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]:MultiValueKey[hostAliases.type,A,(Seq[String],String)] = Key.multiValueKey
  val nodeSelector:"nodeSelector" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]:MultiValueKey[nodeSelector.type,A,(String,String)] = Key.multiValueKey
  val restartPolicy: "restartPolicy" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]:SingleValueKey[restartPolicy.type,A,"Always"|"OnFailure"|"Never"] = Key.singleValueKey
  val volumeEmptyDir: "volumeEmptyDir" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]:MultiValueKey[volumeEmptyDir.type,A,String] = Key.multiValueKey
  val volumeConfigMap: "volumeConfigMap" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]: MultiValueKey[volumeConfigMap.type,A,(String,String)] = Key.multiValueKey
  val containers:"containers" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]: MultiNodeKey[containers.type,A,containers.type] = Key.multiNodeKey
  val initContainers:"initContainers" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]: MultiNodeKey[initContainers.type,A,containers.type] = Key.multiNodeKey
  val backoffLimit:"backoffLimit" = compiletime.constValue
  given [A <: _ >> Job.type  >> spec.type]: SingleValueKey[backoffLimit.type,A,Int] = Key.singleValueKey
  val schedule: "schedule" = compiletime.constValue
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[schedule.type,A,String] = Key.singleValueKey
  val storageClassName: "storageClassName" = compiletime.constValue
  given [A <: _ >> PersistentVolumeClaim.type >> spec.type]: SingleValueKey[storageClassName.type,A,String] = Key.singleValueKey
  val accessModes: "accessModes" = compiletime.constValue
  given [A <: _ >> PersistentVolumeClaim.type >> spec.type]: MultiValueKey[accessModes.type,A,"ReadWriteOnce"|"ReadOnlyMany"|"ReadWriteMany"|"ReadWriteOncePod"] = Key.multiValueKey
  val replicas: "replicas" = compiletime.constValue
  given [A <: _ >> Deployment.type >> spec.type]: SingleValueKey[replicas.type,A,Int] = Key.singleValueKey
  val suspend: "suspend" = compiletime.constValue
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[suspend.type,A,Boolean] = Key.singleValueKey
  val jobTemplate: "jobTemplate" = compiletime.constValue
  given [A <: _ >> CronJob.type >> spec.type]: SingleNodeKey[jobTemplate.type,A,Job.type] = Key.singleNodeKey
  val failedJobsHistoryLimit: "failedJobsHistoryLimit" = compiletime.constValue
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[failedJobsHistoryLimit.type,A,Int] = Key.singleValueKey
  val successfulJobsHistoryLimit: "successfulJobsHistoryLimit" = compiletime.constValue
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[successfulJobsHistoryLimit.type,A,Int] = Key.singleValueKey
  val data: "data" = compiletime.constValue
  given [A <: _ >> ConfigMap.type]: SingleValueKey[data.type,A,Map[String,String]] = Key.singleValueKey
  val volumePVC: "volumePVC" = compiletime.constValue
  given [A <: _ >> Pod.type >> spec.type]: MultiValueKey[volumePVC.type,A,(String,String)] = Key.multiValueKey
  val livenessProbe: "livenessProbe" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleNodeKey[livenessProbe.type,A,livenessProbe.type] = Key.singleNodeKey
  val readinessProbe: "readinessProbe" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleNodeKey[readinessProbe.type,A,livenessProbe.type] = Key.singleNodeKey
  val startupProbe: "startupProbe" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleNodeKey[startupProbe.type,A,livenessProbe.type] = Key.singleNodeKey
  val initialDelaySeconds:"initialDelaySeconds" = compiletime.constValue // 容器调度多久后开始探测？
  given [A <: _ >> livenessProbe.type]: SingleValueKey[initialDelaySeconds.type,A,Int] = Key.singleValueKey
  val periodSeconds:"periodSeconds" = compiletime.constValue // 多久探测一次？
  given [A <: _ >> livenessProbe.type]: SingleValueKey[periodSeconds.type,A,Int] = Key.singleValueKey
  val timeoutSeconds:"timeoutSeconds" = compiletime.constValue // 超时时间
  given [A <: _ >> livenessProbe.type]: SingleValueKey[timeoutSeconds.type,A,Int] = Key.singleValueKey
  val successThreshold:"successThreshold" = compiletime.constValue // 连续成功几次才算成功？
  given [A <: _ >> livenessProbe.type]: SingleValueKey[successThreshold.type,A,Int] = Key.singleValueKey
  val failureThreshold:"failureThreshold" = compiletime.constValue // 连续失败几次才算失败？
  given [A <: _ >> livenessProbe.type]: SingleValueKey[failureThreshold.type,A,Int] = Key.singleValueKey
  val action:"action" = compiletime.constValue
  enum Action:
    case Exec(cmd:String*)
    case HttpGet(path:String,port:Int,host:String|Null = null,scheme:"HTTP"|"HTTPS" = "HTTP",headers:Map[String,String] = Map())
  given [A <: _ >> livenessProbe.type]: SingleValueKey[action.type,A,Action] = Key.singleValueKey
  val tcpSocket: "tcpSocket" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleValueKey[tcpSocket.type,A,Int] = Key.singleValueKey
  val workingDir:"workdingDir" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleValueKey[workingDir.type,A,Int] = Key.singleValueKey
  val imagePullPolicy: "imagePullPolicy" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleValueKey[imagePullPolicy.type,A,"Always"|"IfNotPresent"|"Never"] = Key.singleValueKey
  val command: "command" = compiletime.constValue
  given [A <: _ >> containers.type]: MultiValueKey[command.type,A,String]  = Key.multiValueKey
  val env: "env" = compiletime.constValue
  given [A <: _ >> containers.type]: MultiValueKey[env.type,A,(String,String)] = Key.multiValueKey
  val envFromConfigMapKey: "envFromConfigMapKey" = compiletime.constValue
  given [A <: _ >> containers.type]: MultiValueKey[envFromConfigMapKey.type,A,(String,(String,String))] = Key.multiValueKey
  val envFromSecretKey: "envFromSecretKey" = compiletime.constValue
  given [A <: _ >> containers.type]: MultiValueKey[envFromSecretKey.type,A,(String,(String,String))] = Key.multiValueKey
  val volumeMounts: "volumeMounts" = compiletime.constValue
  given [A <: _ >> containers.type]: MultiValueKey[volumeMounts.type,A,(String,String)] = Key.multiValueKey
  val resources: "resources" = compiletime.constValue
  given [A <: _ >> containers.type]: SingleNodeKey[resources.type,A,resources.type] = Key.singleNodeKey
  val cpu: "cpu" = compiletime.constValue
  case class Cpu(request:Double,limit:Double)
  given [A <: _ >> containers.type >> resources.type]: SingleValueKey[cpu.type,A,Cpu] = Key.singleValueKey
  val memory: "memory" = compiletime.constValue
  case class Memory(request:Int,limit:Int)
  given [A <: _ >> containers.type >> resources.type]: SingleValueKey[memory.type,A,Memory] = Key.singleValueKey
  val storage: "storage" = compiletime.constValue
  given [A <: _ >> PersistentVolumeClaim.type >> spec.type]: SingleValueKey[storage.type,A,Int] = Key.singleValueKey
  val tcpPorts: "tcpPorts" = compiletime.constValue
  given [A <: _ >> Service.type >> spec.type] : MultiValueKey[tcpPorts.type,A,(Int,Int)] = Key.multiValueKey
  val udpPorts: "udpPorts" = compiletime.constValue
  given [A <: _ >> Service.type >> spec.type] : MultiValueKey[udpPorts.type,A,(Int,Int)] = Key.multiValueKey
  val sctpPorts: "sctpPorts" = compiletime.constValue
  given [A <: _ >> Service.type >> spec.type] : MultiValueKey[sctpPorts.type,A,(Int,Int)] = Key.multiValueKey
  val ports: "ports" = compiletime.constValue
  given [A <: _ >> containers.type]: MultiValueKey[ports.type,A,Int] = Key.multiValueKey
  val image: "image" = compiletime.constValue
  given [A <: _ >> containers.type] : SingleValueKey[image.type,A,String] = Key.singleValueKey
  def context(contextName:String,action:"apply"|"delete"|"create"|Null = null)(closure: Scope.Root ?=> Unit):Unit = 
    val x = obj(closure)
    println(x.toHashMap)
object SampleTest:
  @main def main: Unit = 
    import kubernetes.{*,given}
    context("orbstack"):
      Namespace:
        name := "default"
        PersistentVolumeClaim:
          name := "123"
          spec(storageClassName := "storageClassName",accessModes += "ReadWriteOnce")
        ConfigMap:
          name := ""
          data := Map(
             "application.yml" -> raw"""
               |spring:
               |  datasource:
               |    url: qwejqwiehkwehdqwliejo
               |""".stripMargin.stripTrailing().nn.stripLeading().nn
          )
        Pod:
          name := "123"
          labels += "1" -> "2"
          spec:
            println()
            containers(name := "",image := "")
        CronJob:
          name := "123"
          spec:
            suspend := false
            failedJobsHistoryLimit := 3
            successfulJobsHistoryLimit := 4
            jobTemplate:
              spec:
                backoffLimit := 3
                template:
                  labels += "app" -> "123"
        Job:
          name := "123"
          spec(backoffLimit := 3)
        Deployment:
          name := "nginx"
          spec:
            selector += "app" -> "nginx"
            template:
              labels += "app" -> "nginx"
              spec:
                restartPolicy := "Never"
                hostAliases += Seq("www.baidu.com") -> "192.168.50.1"
                volumeEmptyDir += "volumeName0"
                volumeConfigMap += "volumeName1" -> "config-map-name"
                volumePVC += "volumeName2" -> "pvc-name"
                initContainers:
                  livenessProbe(
                    initialDelaySeconds := 3,
                    action := Action.Exec("curl -x 'localhost:8080/api/healthy'")
                    )
                  env += "spring.profiles.active" -> "prod"
                  ports += 80
                containers:
                  println()