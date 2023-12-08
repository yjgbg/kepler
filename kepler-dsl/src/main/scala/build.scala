package com.github.yjgbg.kepler.dsl
object build:
  export core.{*,given}  
  val project:"project" = compiletime.constValue
  inline def rootProject(args:Seq[String])(closure:Root >> project.type ?=> Unit):Unit = {
    val scope:Root >> project.type = Scope.>>(collection.mutable.HashMap())
    closure(using scope)
    if args.length >= 2 then {
      val proj = args.head.split(":").nn.map(_.nn).foldLeft[_ >> project.type](scope)((s,c) => s.get(project).filter(it => it.get(name).filter(_ == c).isDefined).head)
      val ac = proj.get(task).toSeq.filter(_.get(name).exists(_ == args.tail.head)).head.get(action).get
      ac(proj.asInstanceOf,args.tail.tail)
    } else if args.length == 1 then {
       val ac = scope.get(task).toSeq.filter(_.get(name).exists(_ == args.head)).head.get(action).get
       ac(scope.asInstanceOf,args.tail)
    } else throw IllegalArgumentException()
  }
  given [A <: _ >> project.type]:MultiNodeKey[project.type,A,project.type] = Key.multiNodeKey
  val name: "name" = compiletime.constValue
  given [A <: _ >> project.type| _ >> task.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  val organization: "organization" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[organization.type,A,String] = Key.singleValueKey
  val task: "task" = compiletime.constValue
  given [A <: _ >> project.type]:MultiNodeKey[task.type,A,task.type] = Key.multiNodeKey
  val action: "action" = compiletime.constValue
  given [A <: _ >> project.type]:SingleValueKey[action.type,A >> task.type,(A,Seq[String]) => Unit] = Key.singleValueKey
  val dependsOn: "dependsOn" = compiletime.constValue
  given [A <: 
    _ >> project.type
    | _ >> task.type
    ]:MultiValueKey[dependsOn.type,A,String] = Key.multiValueKey
  val library: "library" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[library.type,A,String] = Key.multiValueKey
  val repository: "repository" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[repository.type,A,String] = Key.multiValueKey
  val sourceSets: "sourceSets" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[sourceSets.type,A,String] = Key.multiValueKey
  val resourceSets: "resourceSets" = compiletime.constValue
  given [A <: _ >> project.type]:MultiValueKey[resourceSets.type,A,String] = Key.multiValueKey
  type Plugin = _ >> project.type ?=> Unit
object buildSample:
  import build.{*,given}
  @main def main(args:String*) = rootProject(args):
    name := "rootProject"
    repository += "https://"
    organization := "com.github.yjgbg"
    sourceSets += "src/main/scala"
    sourceSets += "src/main/java"
    resourceSets += "src/main/resources"
    task:
      name := "printName"
      dependsOn += "另一个task的名字"
      action := { (p,args) => p.get(name) match
          case None => println()
          case Some(value) => println(value)
      }
    project:
      name := "kepler-dsl"
      project(name := "kepler-dsl-core")
      library += "com.github.yjgbg::kepler-dsl:1.0.0"
      task:
        name := "printName"
        dependsOn += "另一个task的名字"
        action := { (p,args) => p.get(name) match
            case None => println()
            case Some(value) => println(value)
        }
    project:
      name := "kepler-json-dsl"
      dependsOn += "kepler-dsl"
      task:
        name := "build"

