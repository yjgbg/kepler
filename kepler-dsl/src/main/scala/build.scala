package com.github.yjgbg.kepler.dsl

object build:
  export core.{*,given}
  trait Plugin:
    def apply(using _ >> project.type): Unit
  object Plugin:
    def apply(closure: _ >> project.type ?=> Unit) = new Plugin:
      override def apply(using ? >> project.type): Unit = closure.apply
  val project:"project" = compiletime.constValue
  given [A <: _ >> project.type]:MultiNodeKey[project.type,A,project.type] = Key.multiNodeKey
  val name: "name" = compiletime.constValue
  given [A <: _ >> project.type| _ >> task.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  val version: "version" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[version.type,A,String] = Key.singleValueKey
  val task: "task" = compiletime.constValue
  given [A <: _ >> project.type]:MultiNodeKey[task.type,A,task.type] = Key.multiNodeKey
  val action: "action" = compiletime.constValue
  given [A <: _ >> task.type]:SingleValueKey[action.type,A,_ >> project.type => Unit] = Key.singleValueKey
  val dependsOn: "dependsOn" = compiletime.constValue
  given [A <: 
    _ >> project.type
    | _ >> task.type
    ]:MultiValueKey[dependsOn.type,A,String] = Key.multiValueKey
  val scalacOption:"scalacOption" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[scalacOption.type,A,String] = Key.multiValueKey
  val source: "source" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[source.type,A,String] = Key.multiValueKey
  val target: "target" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[target.type,A,String] = Key.singleValueKey
  private def executeTask(p: _ >> project.type,args:Seq[String]):Unit = 
    if (args.isEmpty) println("no such task to execute")
    else p.get(task).filter(it => it.get(name).get == args.head).headOption match
      case Some(value) => value.get(action).foreach:ac => 
        value.get(dependsOn).foreach:n => 
          executeTask(p,Seq(n))
        ac(p.asInstanceOf)
      case None => p.get(project).filter(it => it.get(name).get == args.head).headOption match
        case Some(value) => executeTask(value,args.tail)
        case None => println("no such task to execute")
  inline def rootProject(args:Seq[String])(using sourcecode.File)(closure:Root >> project.type ?=> Unit):Unit =
    rootProject(summon,args,closure)
  def rootProject(s:sourcecode.File,args:Seq[String],closure:Root >> project.type ?=> Unit):Unit = {
    given scope: >>[Root,project.type] = Scope.>>(collection.mutable.HashMap())
    closure.apply
    Plugin:
      task(name := "bsp-init"):
        action := {p => 
          val seq = ProcessHandle.current().nn.info().nn.commandLine().nn.get().nn.split(" ").nn.toSeq.nn
          val argv = (seq.init :+ "bsp-daemon").map(str => s"\"$str\"").mkString(",")
          import java.nio.file.{Files,Paths}
          Files.writeString(Paths.get(s"./.bsp/${p.get(name).get}.json"),raw"""
          |{
          |  "name": "kepler",
          |  "version": "1.1.0",
          |  "bspVersion": "2.1.0-M7",
          |  "languages": ["scala","java"],
          |  "argv": [$argv]
          |}
          |""".stripMargin.stripTrailing().nn.stripLeading()) 
        }
      task(name := "bsp-daemon"):
        action := {p =>
          var o = ""
          var n = java.nio.file.Files.readString(java.nio.file.Paths.get(s.value)).nn
          var p:scala.sys.process.Process|Null = null
          while (true) {
            if (o != n)
              import scala.sys.process.*
              s"./scala ${s.value} -- bsp-init".!
              if p!=null then p.destroy()
              p = java.lang.ProcessBuilder("./scala",s.value,"--","bsp").inheritIO().nn.run()
              o = n
            else
              Thread.sleep(100)
              n = java.nio.file.Files.readString(java.nio.file.Paths.get(s.value)).nn
          }
        }
      task(name := "bsp"):
        action := {p =>
          import ch.epfl.scala.bsp4j.*
          import java.util.concurrent.CompletableFuture
          import scala.jdk.CollectionConverters.{IterableHasAsScala,SeqHasAsJava}
          val localServer = new BuildServer with ScalaBuildServer:
            val rootUri = new java.util.concurrent.atomic.AtomicReference[String]("file:"+java.nio.file.Paths.get(s.value).nn.toAbsolutePath().nn.getParent().nn.toAbsolutePath().nn.toString()+"/")
            private def getProjectByPath(rootOption: Option[_ >> project.type],seq:List[String]):Option[_ >> project.type] = rootOption match
              case None => None
              case Some(root) => seq match
                case Nil => rootOption
                case head :: tail => getProjectByPath(root.get(project).find(_.get(name) == Some(head)),tail)
            override def buildInitialize(params: InitializeBuildParams | Null): CompletableFuture[InitializeBuildResult]|Null = 
              CompletableFuture.supplyAsync:() => 
                println(params)
                val buildServerCapabilities = BuildServerCapabilities()
                val langList = java.util.List.of("java","scala")
                buildServerCapabilities.setCompileProvider(CompileProvider(langList))
                buildServerCapabilities.setTestProvider(TestProvider(langList))
                buildServerCapabilities.setDebugProvider(DebugProvider(langList))
                buildServerCapabilities.setInverseSourcesProvider(true)
                buildServerCapabilities.setDependencySourcesProvider(true)
                buildServerCapabilities.setDependencyModulesProvider(true)
                buildServerCapabilities.setResourcesProvider(true)
                buildServerCapabilities.setOutputPathsProvider(true)
                buildServerCapabilities.setBuildTargetChangedProvider(false)
                buildServerCapabilities.setJvmRunEnvironmentProvider(true)
                buildServerCapabilities.setJvmTestEnvironmentProvider(true)
                buildServerCapabilities.setCargoFeaturesProvider(false)
                buildServerCapabilities.setCanReload(false)
                InitializeBuildResult(
                  params.nn.getDisplayName,
                  params.nn.getVersion,
                  params.nn.getBspVersion,
                  buildServerCapabilities
                )
            override def buildShutdown(): CompletableFuture[Object] | Null =
              CompletableFuture.supplyAsync: () =>
                println("buildShutdown")
                null
            override def buildTargetCleanCache(params: CleanCacheParams | Null): CompletableFuture[CleanCacheResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println(params)
                CleanCacheResult(true)
            override def buildTargetCompile(params: CompileParams | Null): CompletableFuture[CompileResult] | Null =
              CompletableFuture.supplyAsync: () =>
                println(params)
                CompileResult(StatusCode.OK)
            override def buildTargetDependencyModules(params: DependencyModulesParams | Null): CompletableFuture[DependencyModulesResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println(params)
                val x = params.nn.getTargets().nn.asScala.map{t => DependencyModulesItem(t,Seq().asJava)}.toSeq.asJava
                DependencyModulesResult(x)
            override def buildTargetDependencySources(params: DependencySourcesParams | Null): CompletableFuture[DependencySourcesResult] | Null =
              CompletableFuture.supplyAsync: () =>
                println(params)
                val x = params.nn.getTargets().nn.asScala.map{t => DependencySourcesItem(t,Seq().asJava)}.toSeq.asJava
                DependencySourcesResult(x)
            override def buildTargetInverseSources(params: InverseSourcesParams | Null): CompletableFuture[InverseSourcesResult] | Null =
              CompletableFuture.supplyAsync: () =>
                println(params)
                InverseSourcesResult(Seq().asJava)
            override def buildTargetOutputPaths(params: OutputPathsParams | Null): CompletableFuture[OutputPathsResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println(params)
                val x = params.nn.getTargets().nn.asScala.map{t => OutputPathsItem(t,Seq().asJava)}.toSeq.asJava
                OutputPathsResult(x)
            override def buildTargetResources(params: ResourcesParams | Null): CompletableFuture[ResourcesResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println(params)
                val x = params.nn.getTargets().nn.asScala.map{t => ResourcesItem(t,Seq().asJava)}.toSeq.asJava
                ResourcesResult(x)
            override def buildTargetRun(params: RunParams | Null): CompletableFuture[RunResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println(params)
                RunResult(StatusCode.OK)
            override def buildTargetSources(params: SourcesParams | Null): CompletableFuture[SourcesResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println(params)
                val x = params.nn.getTargets().nn.asScala.map{t => 
                  val path = t.getUri().nn.split(":").nn.map(_.nn)
                  val op = getProjectByPath(Some(p),path.tail.toList)
                  SourcesItem(t,op.get.get(source).map{x =>
                    SourceItem(rootUri.get().nn+path.tail.mkString("","/","/")+x,SourceItemKind.DIRECTORY,false)
                  }.asJava)
                }.toSeq.asJava
                SourcesResult(x)
            override def buildTargetTest(params: TestParams | Null): CompletableFuture[TestResult] | Null =
              CompletableFuture.supplyAsync: () =>
                println(params)
                TestResult(StatusCode.OK)
            override def debugSessionStart(params: DebugSessionParams | Null): CompletableFuture[DebugSessionAddress] | Null =
              CompletableFuture.supplyAsync: () =>
                println(params)
                DebugSessionAddress("uri")
            override def onBuildExit(): Unit = 
              println("onBuildExit")
            override def onBuildInitialized(): Unit = 
              println("onBuildInitialized")
            override def onRunReadStdin(params: ReadParams | Null): Unit = 
              println(params)
            override def workspaceBuildTargets(): CompletableFuture[WorkspaceBuildTargetsResult] | Null = 
              CompletableFuture.supplyAsync: () =>
                println("workspaceBuildTargets")
                def allBuildTarget(prefix:Seq[String])(using root: ? >> project.type):Seq[BuildTarget] = 
                  val buildTargetCapabilities = new BuildTargetCapabilities()
                  buildTargetCapabilities.setCanCompile(true)
                  buildTargetCapabilities.setCanDebug(false)
                  buildTargetCapabilities.setCanRun(true)
                  buildTargetCapabilities.setCanTest(false)
                  val path = prefix :+ name.get.get
                  val id = path.mkString(":")
                  val buildTarget = BuildTarget(
                    BuildTargetIdentifier(id),
                    java.util.List.of("library","application"),
                    java.util.List.of("scala","java"),
                    Seq().asJava,
                    buildTargetCapabilities
                  )
                  buildTarget.setDataKind("scala")
                  buildTarget.setBaseDirectory(path.tail.mkString(rootUri.get().nn,"/","/"))
                  buildTarget.setDisplayName(name.get.get)
                  buildTarget +: project.get.flatMap(newP => allBuildTarget(path)(using newP.asInstanceOf))
                import scala.jdk.CollectionConverters.SeqHasAsJava
                WorkspaceBuildTargetsResult(allBuildTarget(Seq())(using p.asInstanceOf).asJava)
            override def workspaceReload(): CompletableFuture[Object] | Null = 
              CompletableFuture.supplyAsync: () =>
                println("workspaceReload")
                null
            override def buildTargetScalacOptions(params: ScalacOptionsParams | Null): CompletableFuture[ScalacOptionsResult] | Null = 
              CompletableFuture.supplyAsync: () => 
                val x = params.nn.getTargets().nn.asScala.map{t => 
                  val path = t.getUri().nn.split(":").nn.map(_.nn)
                  val sc = getProjectByPath(Some(p),path.tail.toList) match
                    case None => Seq()
                    case Some(value) => value.get(scalacOption)
                  ScalacOptionsItem(t,sc.asJava,Seq().asJava,rootUri.get().nn + path.mkString("/"))
                }.toSeq.asJava
                ScalacOptionsResult(x)
            override def buildTargetScalaMainClasses(params: ScalaMainClassesParams | Null): CompletableFuture[ScalaMainClassesResult] | Null = 
              CompletableFuture.supplyAsync: () => 
                val x = params.nn.getTargets().nn.asScala.map(ScalaMainClassesItem(_,Seq().asJava)).toSeq.asJava
                ScalaMainClassesResult(x)
            override def buildTargetScalaTestClasses(params: ScalaTestClassesParams | Null): CompletableFuture[ScalaTestClassesResult] | Null = 
              CompletableFuture.supplyAsync: () => 
                val x = params.nn.getTargets().nn.asScala.map(ScalaTestClassesItem(_,Seq().asJava)).toSeq.asJava
                ScalaTestClassesResult(x)
          val launcher = new org.eclipse.lsp4j.jsonrpc.Launcher.Builder[BuildClient]()
            .setOutput(System.out).nn
            .setInput(System.in).nn
            .setLocalService(localServer).nn
            .setRemoteInterface(classOf[BuildClient]).nn
            .create().nn
          println("bsp started")
          launcher.startListening().nn.get().nn
        }
    .apply
    executeTask(scope,args)
}