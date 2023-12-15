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
  val sourceSets: "sourceSets" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[sourceSets.type,A,String] = Key.multiValueKey
  val target: "target" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[target.type,A,String] = Key.singleValueKey
  def rootProject(args:Seq[String])(closure:Root >> project.type ?=> Unit):Unit = {
    if (args.isEmpty) println("no task to execute")
    given scope: >>[Root,project.type] = Scope.>>(collection.mutable.HashMap())
    closure.apply
    Plugin:
      sourceSets += "src"
      target := "target"
      task(name := "~"):
        action := {p =>
          val newCmdLine = ProcessHandle.current().nn.info().nn.commandLine().nn.get().nn
            .replaceAll("~[ \t]*"+args.tail.mkString("[ \t]*"),args.tail.mkString(" ")).nn
          println(newCmdLine)
          import scala.sys.process.*
          System.exit(newCmdLine.!)
        }
    .apply
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
object buildSample:
  import build.{*,given}
  @main def main(args:String*) = rootProject(args):
    val printName = Plugin:
      task(name := "printName"):
        // dependsOn += "另一个task的名字"
        action := { p => p.get(name) match
          case None => println()
          case Some(value) => println(value)
        }
    name := "root"
    printName.apply
    project(name := "kepler-dsl"):
      printName.apply
      project(name := "kepler-dsl-core"):
        printName.apply