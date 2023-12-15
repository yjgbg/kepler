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
  given [A <: _ >> project.type| _ >> project.type >> task.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  val task: "task" = compiletime.constValue
  given [A <: _ >> project.type]:MultiNodeKey[task.type,A,task.type] = Key.multiNodeKey
  val action: "action" = compiletime.constValue
  given [A <: _ >> task.type]:SingleValueKey[action.type,A,A => Unit] = Key.singleValueKey
  val dependsOn: "dependsOn" = compiletime.constValue
  given [A <: 
    _ >> project.type
    | _ >> project.type >> task.type
    ]:MultiValueKey[dependsOn.type,A,String] = Key.multiValueKey
  val source: "source" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[source.type,A,String] = Key.multiValueKey
  val target: "target" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[target.type,A,String] = Key.singleValueKey
  private def bsp = Plugin:
    task(name := "bsp-init"):
      action := {p => 
        val seq = ProcessHandle.current().nn.info().nn.commandLine().nn.get().nn.split(" ").nn.toSeq.nn
        val argv = (seq.init :+ "bsp").map(str => s"\"$str\"").mkString(",")
        import java.nio.file.{Files,Paths}
        Files.writeString(Paths.get("./.bsp/build.json"),raw"""
        |{
        |  "name": "build",
        |  "version": "1.8.0",
        |  "bspVersion": "2.1.0-M1",
        |  "languages": [
        |    "scala"
        |  ],
        |  "argv": [$argv]
        |}
        |""".stripMargin.stripTrailing().nn.stripLeading()) 
      }
    task(name := "bsp"):
      action := {p =>
        import ch.epfl.scala.bsp4j.*
        import java.util.concurrent.CompletableFuture
        val localServer = new BuildServer:
          override def buildInitialize(params: InitializeBuildParams | Null): CompletableFuture[InitializeBuildResult] = ???
          override def buildShutdown(): CompletableFuture[Object] | Null = ???
          override def buildTargetCleanCache(params: CleanCacheParams | Null): CompletableFuture[CleanCacheResult] | Null = ???
          override def buildTargetCompile(params: CompileParams | Null): CompletableFuture[CompileResult] | Null = ???
          override def buildTargetDependencyModules(params: DependencyModulesParams | Null): CompletableFuture[DependencyModulesResult] | Null = ???
          override def buildTargetDependencySources(params: DependencySourcesParams | Null): CompletableFuture[DependencySourcesResult] | Null = ???
          override def buildTargetInverseSources(params: InverseSourcesParams | Null): CompletableFuture[InverseSourcesResult] | Null = ???
          override def buildTargetOutputPaths(params: OutputPathsParams | Null): CompletableFuture[OutputPathsResult] | Null = ???
          override def buildTargetResources(params: ResourcesParams | Null): CompletableFuture[ResourcesResult] | Null = ???
          override def buildTargetRun(params: RunParams | Null): CompletableFuture[RunResult] | Null = ???
          override def buildTargetSources(params: SourcesParams | Null): CompletableFuture[SourcesResult] | Null = ???
          override def buildTargetTest(params: TestParams | Null): CompletableFuture[TestResult] | Null = ???
          override def debugSessionStart(params: DebugSessionParams | Null): CompletableFuture[DebugSessionAddress] | Null = ???
          override def onBuildExit(): Unit = ???
          override def onBuildInitialized(): Unit = ???
          override def onRunReadStdin(params: ReadParams | Null): Unit = ???
          override def workspaceBuildTargets(): CompletableFuture[WorkspaceBuildTargetsResult] | Null = ???
          override def workspaceReload(): CompletableFuture[Object] | Null = ???
        val launcher = new org.eclipse.lsp4j.jsonrpc.Launcher.Builder[BuildClient]()
          .setOutput(System.out).nn
          .setInput(System.in).nn
          .setLocalService(localServer).nn
          .setRemoteInterface(classOf[BuildClient]).nn
          .create().nn
        println("bsp started")
        launcher.startListening().nn.get().nn
      }
    
  def rootProject(args:Seq[String])(closure:Root >> project.type ?=> Unit):Unit = {
    if (args.isEmpty) println("no task to execute")
    else 
      given scope: >>[Root,project.type] = Scope.>>(collection.mutable.HashMap())
      closure.apply
      Plugin:
        source += "src"
        target := "target"
        task(name := "~"):
          action := {p =>
            val newCmdLine = ProcessHandle.current().nn.info().nn.commandLine().nn.get().nn
              .replaceAll("~[ \t]*"+args.tail.mkString("[ \t]*"),args.tail.mkString(" ")).nn
            import scala.sys.process.*
            System.exit(newCmdLine.!)
          }
      .apply
      bsp.apply
      def executeTask(p: _ >> project.type,args:Seq[String]):Unit = 
        p.get(task).filter(it => it.get(name).get == args.head).headOption match
          case Some(value) => value.get(action).foreach:ac => 
            value.get(dependsOn).foreach:n => 
              executeTask(p,Seq(n))
            ac(p.asInstanceOf)
          case None => p.get(project).filter(it => it.get(name).get == args.head).headOption match
            case Some(value) => executeTask(value,args.tail)
            case None => println("no task to execute")
      executeTask(scope,args)
  }