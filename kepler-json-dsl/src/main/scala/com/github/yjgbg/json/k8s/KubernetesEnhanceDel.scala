package com.github.yjgbg.json.k8s
object KubernetesEnhanceDel extends KubernetesDsl,KubernetesEnhanceDel
trait KubernetesEnhanceDel:
  self: KubernetesDsl =>
  case class UtilityImage(var image:String)
  given UtilityImage = UtilityImage("alpine:latest")
  def utilityImage(image:String):Unit = summon[UtilityImage].image = image
  private[KubernetesEnhanceDel] case class ImagePathFile(key:String, image:String, path:String, initScripts:Seq[String])
  private[KubernetesEnhanceDel] case class LiteralTextFile(key:String, text:String)
  class VolumeCustomScope {
    private[KubernetesEnhanceDel] var imagePathFileSeq:Seq[ImagePathFile] = Seq()
    private[KubernetesEnhanceDel] var literalTextFileSeq:Seq[LiteralTextFile] = Seq()
  }
  /**
    * 创建一个自定义卷
    *
    * @param name 卷名
    * @param closure
    */
  def volumeCustom(using PodScope >> SpecScope)(name:String)(closure:VolumeCustomScope ?=> Unit) = {
    val vcs:VolumeCustomScope = VolumeCustomScope()
    closure(using vcs)
    volumeEmptyDir(name)
    
    val atomicInt = new java.util.concurrent.atomic.AtomicInteger(0)
    if (!vcs.literalTextFileSeq.isEmpty) initContainer(name+"-"+atomicInt.getAndAdd(1),summon[UtilityImage].image) {
      imagePullPolicy("IfNotPresent")
      volumeMounts(name -> "/literal")
      val variableNameAndLiteralTextFileSeq = vcs.literalTextFileSeq.distinctBy(_.key)
        .zipWithIndex.map((ltf,i) => ("variable_"+i.toString(),ltf))
      variableNameAndLiteralTextFileSeq.foreach{(vn,ltf) => env(vn -> ltf.text)}
      command("sh","-c",variableNameAndLiteralTextFileSeq
        .map{(vn,ltf) => s"""echo "${"$"}{$vn}" > /literal/${ltf.key};chmod 777 /literal/${ltf.key}"""}
        .mkString("\n"))
    }
    vcs.imagePathFileSeq.foreach { case ImagePathFile(key,image,path,initScripts) => 
      initContainer(key+"-"+atomicInt.getAndAdd(1),image) {
        imagePullPolicy("IfNotPresent")
        volumeMounts(name -> s"/tmp/vol")
        initScripts.zipWithIndex.foreach {(it,index) => env(s"INIT_$index" -> it)}
        command("sh","-c",
          s"""|
          |rm -rf /tmp/vol/$key
          |${(0 until initScripts.length).map{it => s"echo '${"$"}INIT_$it'|sh"}.mkString("\n")}
          |mkdir -p /tmp/vol
          |cp -a $path /tmp/vol/$key
          |""".stripMargin.nn.stripLeading().nn.stripTrailing().nn
        )
      }
    }
  }
  /**
    * 声明一个自定义文本文件，并且会赋予777权限
    *
    * @param fileName 文件在卷中的名字
    * @param content 文件文本内容
    */
  def fileLiteralText(using VolumeCustomScope)(fileName:String,content:String):Unit = 
    summon[VolumeCustomScope].literalTextFileSeq = 
      summon[VolumeCustomScope].literalTextFileSeq :+ LiteralTextFile(fileName,content)
  /**
    * 声明一个来自于镜像的文件
    *
    * @param fileName 文件名在卷中的名字
    * @param image 文件所在的镜像
    * @param path 文件在镜像中所在的目录
    */
  def fileImagePath(using VolumeCustomScope)(fileName:String,image:String,path:String,scripts:String*):Unit =
    summon[VolumeCustomScope].imagePathFileSeq = 
      summon[VolumeCustomScope].imagePathFileSeq :+ ImagePathFile(fileName,image,path,scripts)
  /**
    *  创建一个到远程服务器的代理
    *
    * @param ip 远程服务器的ip地址
    * @param port 要代理的远程服务器的端口号
    * @param localPort 本地端口，可以不写，会用远程端口号
    * @param image 镜像，如果集群可以访问dockerhub也不建议写
    * @param closure 对容器的其他配置
    */
  def proxy(using PodScope >> SpecScope)(
    ip:String,
    port:Int,
    localPort:Int|Null = null,
    image:String = "marcnuri/port-forward"
  )(closure: PodScope >> SpecScope >> ContainerScope ?=> Unit) = {
    val localPort0 = (if localPort != null then localPort else port).toString()
    container(s"proxy-$localPort0",image) {
      env(
        "REMOTE_HOST" -> ip,
        "REMOTE_PORT" -> port.toString(),
        "LOCAL_PORT" -> localPort0
      )
      closure.apply
    }
  }