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
    closure(using scope)
    val ac = scope.get(task).toSeq.filter(_.get(name).exists(_ == args.head)).headOption.flatMap(_.get(action))
    ac match
      case None => println("no task to execute")
      case Some(value) => value(scope.asInstanceOf,args.tail)
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
// object bsp:
//   import build.{*,given}
//   def apply(using ? >> project.type): build.Plugin = 
//     task: a ?=> 
//       name := "~"
//       action := {(p,args) =>
//         import scala.sys.process.*
//         val commandLine = ProcessHandle.current().nn.info().nn.commandLine().nn.orElse("").nn
//         val cmd = commandLine.replaceFirst(" ~ "," ").nn
//         val p = "".run()
//         System.exit(p.exitValue())
//       }
//     task: a ?=>
//       name := "initBsp"
//       action := {(p,args) => 
//         // 输出startBsp文件到项目跟目录
//       }
//     task: a ?=>
//       name := "bsp"
//       action := {(p,args) => 
//         val thread = new Thread():
//           override def run(): Unit = while (true)
//             println("Hello")
//             Thread.sleep(1000L)
//         thread.start()
//         val scanner = java.util.Scanner(System.in)
//         scanner.nextLine()
//       }
object buildSample:
  import build.{*,given}
  @main def main(args:String*) = rootProject(args):
    // bsp.apply
    // javaPlugin.apply
    // javaPlugin.version := "21"
    task(name := "printName"):
      dependsOn += "另一个task的名字"
      action := { (p,args) => p.get(name) match
        case None => println()
        case Some(value) => println(value)
      }
    project(name := "kepler-dsl"):
      project(name := "kepler-dsl-core")
      task(name := "printName"):
        action := { (p,args) => p.get(name) match
          case None => println()
          case Some(value) => println(value)
        }
