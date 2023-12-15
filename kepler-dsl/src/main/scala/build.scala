package com.github.yjgbg.kepler.dsl

object build:
  export core.{*,given}
  trait Plugin:
    def apply(using _ >> project.type): Unit
  object Plugin:
    def apply(closure: _ >> project.type ?=> Unit) = new Plugin:
      override def apply(using ? >> project.type): Unit = closure.apply
  val project:"project" = compiletime.constValue
  private def ~ = Plugin:
    task(name := "~"):
      action := {(p,args) =>
        val cmdLine = ProcessHandle.current().nn.info().nn.commandLine().nn.get().nn
        val newCmdLine = cmdLine.replaceAll("~[ \t]*"+args.mkString("[ \t]*"),args.mkString(" ")).nn
        import scala.sys.process.*
        System.exit(newCmdLine.!)
      }
  def rootProject(args:Seq[String])(closure:Root >> project.type ?=> Unit):Unit = {
    if (args.isEmpty) println("no task to execute")
    given scope: >>[Root,project.type] = Scope.>>(collection.mutable.HashMap())
    ~.apply
    closure.apply
    def executeTask(p: _ >> project.type,args:Seq[String]):Unit = 
      p.get(task).filter(it => it.get(name).get == args.head).headOption match
        case Some(value) => value.get(action).foreach(_(p.asInstanceOf,args.tail))
        case None => p.get(project).filter(it => it.get(name).get == args.head).headOption match
          case Some(value) => executeTask(value,args.tail)
          case None => println("no task to execute")
    executeTask(scope,args)
  }
  given [A <: _ >> project.type]:MultiNodeKey[project.type,A,project.type] = Key.multiNodeKey
  val name: "name" = compiletime.constValue
  given [A <: _ >> project.type| _ >> project.type >> task.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  val task: "task" = compiletime.constValue
  given [A <: _ >> project.type]:MultiNodeKey[task.type,A,task.type] = Key.multiNodeKey
  val action: "action" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[action.type,A >> task.type,(A,Seq[String]) => Unit] = Key.singleValueKey
  val dependsOn: "dependsOn" = compiletime.constValue
  given [A <: 
    _ >> project.type
    | _ >> project.type >> task.type
    ]:MultiValueKey[dependsOn.type,A,String] = Key.multiValueKey
object buildSample:
  import build.{*,given}
  val printName = Plugin:
    task(name := "printName"):
      dependsOn += "另一个task的名字"
      action := { (p,args) => p.get(name) match
        case None => println()
        case Some(value) => println(value)
      }
  @main def main(args:String*) = rootProject(args):
    name := "root"
    printName.apply
    project(name := "kepler-dsl"):
      printName.apply
      project(name := "kepler-dsl-core"):
        printName.apply