package com.github.yjgbg.kepler.dsl

object kubernetes:
  export core.*
  // 声明Scope和Key字面量
  val Namespace:"Namespace" = compiletime.constValue
  val Deployment:"Deployment" = compiletime.constValue
  val Service:"Service" = compiletime.constValue
  val ConfigMap: "ConfigMap" = compiletime.constValue
  val Secret:"Secret" = compiletime.constValue
  val Pod:"Pod" = compiletime.constValue
  val Job:"Job" = compiletime.constValue
  val name:"name" = compiletime.constValue
  val labels:"labels" = compiletime.constValue
  val annotations:"annotations" = compiletime.constValue
  val spec:"spec" = compiletime.constValue
  val selector:"selector" = compiletime.constValue
  val template:"template" = compiletime.constValue
  val hostAliases:"hostAliases" = compiletime.constValue
  val nodeSelector:"nodeSelector" = compiletime.constValue
  val restartPolicy: "restartPolicy" = compiletime.constValue
  val volumeEmptyDir: "volumeEmptyDir" = compiletime.constValue
  val volumeConfigMap: "volumeConfigMap" = compiletime.constValue
  val volumeCustom: "volumeCustom" = compiletime.constValue
  val fileLiterial: "fileLiterial" = compiletime.constValue
  val fileFromImage: "fileFromImage" = compiletime.constValue
  val containers:"containers" = compiletime.constValue
  val initContainers:"initContainers" = compiletime.constValue
  val backoffLimit:"backoffLimit" = compiletime.constValue
  val CronJob: "CronJob" = compiletime.constValue
  val schedule: "schedule" = compiletime.constValue
  val PersistentVolumeClaim: "PersistentVolumeClaim" = compiletime.constValue
  val storageClassName: "storageClassName" = compiletime.constValue
  val accessModes: "accessModes" = compiletime.constValue
  val replicas: "replicas" = compiletime.constValue
  val suspend: "suspend" = compiletime.constValue
  val jobTemplate: "jobTemplate" = compiletime.constValue
  val failedJobsHistoryLimit: "failedJobsHistoryLimit" = compiletime.constValue
  val successfulJobsHistoryLimit: "successfulJobsHistoryLimit" = compiletime.constValue
  val data: "data" = compiletime.constValue
  val volumePVC: "volumePVC" = compiletime.constValue
  val livenessProbe: "livenessProbe" = compiletime.constValue
  val readinessProbe: "readinessProbe" = compiletime.constValue
  val startupProbe: "startupProbe" = compiletime.constValue
  val initialDelaySeconds:"initialDelaySeconds" = compiletime.constValue // 容器调度多久后开始探测？
  val periodSeconds:"periodSeconds" = compiletime.constValue // 多久探测一次？
  val timeoutSeconds:"timeoutSeconds" = compiletime.constValue // 超时时间
  val successThreshold:"successThreshold" = compiletime.constValue // 连续成功几次才算成功？
  val failureThreshold:"failureThreshold" = compiletime.constValue // 连续失败几次才算失败？
  val action:"action" = compiletime.constValue
  val tcpSocket: "tcpSocket" = compiletime.constValue
  val workingDir:"workdingDir" = compiletime.constValue
  val imagePullPolicy: "imagePullPolicy" = compiletime.constValue
  val command: "command" = compiletime.constValue
  val env: "env" = compiletime.constValue
  val envFromConfigMapKey: "envFromConfigMapKey" = compiletime.constValue
  val envFromSecretKey: "envFromSecretKey" = compiletime.constValue
  val volumeMounts: "volumeMounts" = compiletime.constValue
  val resources: "resources" = compiletime.constValue
  val cpu: "cpu" = compiletime.constValue
  val memory: "memory" = compiletime.constValue
  val storage: "storage" = compiletime.constValue
  val tcpPorts: "tcpPorts" = compiletime.constValue
  val udpPorts: "udpPorts" = compiletime.constValue
  val sctpPorts: "sctpPorts" = compiletime.constValue
  val ports: "ports" = compiletime.constValue
  val image: "image" = compiletime.constValue
  // 定义key和node的关系
  // Namespace是一个定义在Root Scope上的key,会创造出一个Root >> Namespace的Scope
  given MultiNodeKey[Namespace.type,Scope.Root,Namespace.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[Deployment.type,A,Deployment.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[Service.type,A,Service.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[ConfigMap.type,A,ConfigMap.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[Secret.type,A,Secret.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[Pod.type,A,Pod.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[Job.type,A,Job.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[CronJob.type,A,CronJob.type] = Key.multiNodeKey
  given [A <: _ >> Namespace.type]:MultiNodeKey[PersistentVolumeClaim.type,A,PersistentVolumeClaim.type] = Key.multiNodeKey
  given [A<: 
    _ >> Namespace.type
    | _ >> Deployment.type
    | _ >> Service.type
    | _ >> ConfigMap.type
    | _ >> Secret.type
    | _ >> Pod.type
    | _ >> Job.type
    | _ >> CronJob.type
    | _ >> PersistentVolumeClaim.type
    | _ >> containers.type
    | _ >> volumeCustom.type
    ]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  given [A<: 
    _ >> Deployment.type
    | _ >> Service.type
    | _ >> ConfigMap.type
    | _ >> Secret.type
    | _ >> Pod.type
    | _ >> Job.type
    | _ >> CronJob.type
    | _ >> PersistentVolumeClaim.type
    ]:MultiValueKey[labels.type,A,(String,String)] = Key.multiValueKey
  given [A<: _ >>
    _ >> Deployment.type
    | _ >> Service.type
    | _ >> ConfigMap.type
    | _ >> Secret.type
    | _ >> Pod.type
    | _ >> Job.type
    | _ >> CronJob.type
    | _ >> PersistentVolumeClaim.type
   ]:MultiValueKey[annotations.type,A,(String,String)] = Key.multiValueKey
  given [A<:
    _ >> Deployment.type
    | _ >> Service.type
    | _ >> ConfigMap.type
    | _ >> Secret.type
    | _ >> Pod.type
    | _ >> Job.type
    | _ >> CronJob.type
    | _ >> PersistentVolumeClaim.type
    ]:SingleNodeKey[spec.type,A,spec.type] = Key.singleNodeKey
  given [A<:
    _ >> Deployment.type >> spec.type
    | _ >> Service.type >> spec.type
  ]:MultiValueKey[selector.type,A,(String,String)] = Key.multiValueKey
  given [A <: 
    _ >> Deployment.type >> spec.type
    | _ >> Job.type >> spec.type
    ]:SingleNodeKey[template.type,A,Pod.type] = Key.singleNodeKey
  given [A <: _ >> Pod.type >> spec.type]:MultiValueKey[hostAliases.type,A,(Seq[String],String)] = Key.multiValueKey
  given [A <: _ >> Pod.type >> spec.type]:MultiValueKey[nodeSelector.type,A,(String,String)] = Key.multiValueKey
  given [A <: _ >> Pod.type >> spec.type]:SingleValueKey[restartPolicy.type,A,"Always"|"OnFailure"|"Never"] = Key.singleValueKey
  given [A <: _ >> Pod.type >> spec.type]:MultiValueKey[volumeEmptyDir.type,A,String] = Key.multiValueKey
  given [A <: _ >> Pod.type >> spec.type]: MultiValueKey[volumeConfigMap.type,A,(String,String)] = Key.multiValueKey
  given [A <: _ >> Pod.type >> spec.type]: MultiNodeKey[containers.type,A,containers.type] = Key.multiNodeKey
  given [A <: _ >> Pod.type >> spec.type]: MultiNodeKey[initContainers.type,A,containers.type] = Key.multiNodeKey
  given [A <: _ >> Job.type  >> spec.type]: SingleValueKey[backoffLimit.type,A,Int] = Key.singleValueKey
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[schedule.type,A,String] = Key.singleValueKey
  given [A <: _ >> ConfigMap.type]: SingleValueKey[data.type,A,Map[String,String]] = Key.singleValueKey
  given [A <: _ >> PersistentVolumeClaim.type >> spec.type]: SingleValueKey[storageClassName.type,A,String] = Key.singleValueKey
  given [A <: _ >> PersistentVolumeClaim.type >> spec.type]: MultiValueKey[accessModes.type,A,"ReadWriteOnce"|"ReadOnlyMany"|"ReadWriteMany"|"ReadWriteOncePod"] = Key.multiValueKey
  given [A <: _ >> Deployment.type >> spec.type]: SingleValueKey[replicas.type,A,Int] = Key.singleValueKey
  given [A <: _ >> CronJob.type >> spec.type] : SingleValueKey[suspend.type,A,Boolean] = Key.singleValueKey
  given [A <: _ >> CronJob.type >> spec.type]: SingleNodeKey[jobTemplate.type,A,Job.type] = Key.singleNodeKey
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[failedJobsHistoryLimit.type,A,Int] = Key.singleValueKey
  given [A <: _ >> CronJob.type >> spec.type]: SingleValueKey[successfulJobsHistoryLimit.type,A,Int] = Key.singleValueKey
  // 第一个字符串为卷名，第二个字符串为PVC名
  given [A <: _ >> Pod.type >> spec.type]: MultiValueKey[volumePVC.type,A,(String,String)] = Key.multiValueKey
  given [A <: _ >> containers.type]: SingleNodeKey[livenessProbe.type,A,livenessProbe.type] = Key.singleNodeKey
  given [A <: _ >> containers.type]: SingleNodeKey[readinessProbe.type,A,livenessProbe.type] = Key.singleNodeKey
  given [A <: _ >> containers.type]: SingleNodeKey[startupProbe.type,A,livenessProbe.type] = Key.singleNodeKey
  given [A <: _ >> livenessProbe.type]: SingleValueKey[initialDelaySeconds.type,A,Int] = Key.singleValueKey
  given [A <: _ >> livenessProbe.type]: SingleValueKey[periodSeconds.type,A,Int] = Key.singleValueKey
  given [A <: _ >> livenessProbe.type]: SingleValueKey[timeoutSeconds.type,A,Int] = Key.singleValueKey
  given [A <: _ >> livenessProbe.type]: SingleValueKey[successThreshold.type,A,Int] = Key.singleValueKey
  given [A <: _ >> livenessProbe.type]: SingleValueKey[failureThreshold.type,A,Int] = Key.singleValueKey
  enum Action:
    case Exec(cmd:String*)
    case HttpGet(path:String,port:Int,host:String|Null = null,scheme:"HTTP"|"HTTPS" = "HTTP",headers:Map[String,String] = Map())
  given [A <: _ >> livenessProbe.type]: SingleValueKey[action.type,A,Action] = Key.singleValueKey
  given [A <: _ >> containers.type]: SingleValueKey[tcpSocket.type,A,Int] = Key.singleValueKey
  given [A <: _ >> containers.type]: SingleValueKey[workingDir.type,A,Int] = Key.singleValueKey
  given [A <: _ >> containers.type]: SingleValueKey[imagePullPolicy.type,A,"Always"|"IfNotPresent"|"Never"] = Key.singleValueKey
  given [A <: _ >> containers.type]: MultiValueKey[command.type,A,String]  = Key.multiValueKey
  given [A <: _ >> containers.type]: MultiValueKey[env.type,A,(String,String)] = Key.multiValueKey
  given [A <: _ >> containers.type]: MultiValueKey[envFromConfigMapKey.type,A,(String,(String,String))] = Key.multiValueKey
  given [A <: _ >> containers.type]: MultiValueKey[envFromSecretKey.type,A,(String,(String,String))] = Key.multiValueKey
  given [A <: _ >> containers.type]: MultiValueKey[volumeMounts.type,A,(String,String)] = Key.multiValueKey
  given [A <: _ >> containers.type]: SingleNodeKey[resources.type,A,resources.type] = Key.singleNodeKey
  given [A <: _ >> containers.type >> resources.type]: SingleValueKey[cpu.type,A,(Double,Double)] = Key.singleValueKey
  given [A <: _ >> containers.type >> resources.type]: SingleValueKey[memory.type,A,(Int,Int)] = Key.singleValueKey
  given [A <: _ >> PersistentVolumeClaim.type >> spec.type]: SingleValueKey[storage.type,A,Int] = Key.singleValueKey
  given [A <: _ >> Service.type >> spec.type] : MultiValueKey[tcpPorts.type,A,(Int,Int)] = Key.multiValueKey
  given [A <: _ >> Service.type >> spec.type] : MultiValueKey[udpPorts.type,A,(Int,Int)] = Key.multiValueKey
  given [A <: _ >> Service.type >> spec.type] : MultiValueKey[sctpPorts.type,A,(Int,Int)] = Key.multiValueKey
  given [A <: _ >> containers.type]: MultiValueKey[ports.type,A,Int] = Key.multiValueKey
  given [A <: _ >> containers.type] : SingleValueKey[image.type,A,String] = Key.singleValueKey
  given [A <: _ >> Pod.type >> spec.type]: MultiNodeKey[volumeCustom.type,A,volumeCustom.type] = Key.multiNodeKey
  given [A <: _ >> Pod.type >> spec.type >> volumeCustom.type]: MultiValueKey[fileLiterial.type,A,(String,String)] = Key.multiValueKey
  given [A <: _ >> Pod.type >> spec.type >> volumeCustom.type]: MultiValueKey[fileFromImage.type,A,(String,String,String)] = Key.multiValueKey
  // 定义dsl的主函数
  def context(contextName:String,action:"apply"|"delete"|"create"|Null = null)(closure: Scope.Root ?=> Unit):Unit = 
    val x = obj(closure)
    for {
      ns <- x.getMultiNode(Namespace)
      nsName = ns.getSingleValue(name)
    }
    println(x)
    x.getMultiNode(Namespace).foreach: ns =>
      val nsName = ns.getSingleValue(name).getOrElse("default")
      ns.getMultiNode(Deployment).foreach: deploy =>
        val deployName = deploy.getSingleValue(name).get
        val filePath = s"target/$contextName/$nsName-$deployName-deployment.yaml"
        import java.nio.file.{Files,Paths}
        Files.writeString(Paths.get(filePath),"")
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