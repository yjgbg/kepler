package com.github.yjgbg.kepler.dsl

object kubernetes:
  export core.*
  // 声明Scope和Key字面量
  val Namespace:"Namespace" = compiletime.constValue
  val Deployment:"Deployment" = compiletime.constValue
  val Pod:"Pod" = compiletime.constValue
  val name:"name" = compiletime.constValue
  val labels:"labels" = compiletime.constValue
  val spec:"spec" = compiletime.constValue
  val template:"template" = compiletime.constValue
  // 定义key和node的关系
  // Namespace是一个定义在Root Scope上的key,会创造出一个Root >> Namespace的Scope
  given NodeKey[Namespace.type,Scope.Root,Namespace.type] = Key.nodeKey
  given NodeKey[Deployment.type,Scope.Root >> Namespace.type,Deployment.type] = Key.nodeKey
  given NodeKey[Pod.type,Scope.Root >> Namespace.type,Pod.type] = Key.nodeKey
  given [A<: 
    _ >> Namespace.type
    | _ >> Deployment.type
    | _ >> Pod.type
    ]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  given [A<: _ >> Deployment.type| _ >> Pod.type]:MultiValueKey[labels.type,A,(String,String)] = Key.multiValueKey
  given [A<:_ >> Deployment.type|_ >> Pod.type]:NodeKey[spec.type,A,spec.type] = Key.nodeKey
  given [A <: _ >> spec.type]:NodeKey[template.type,A,Pod.type] = Key.nodeKey
  // 定义dsl的主函数
  def context(name0:String,action0:"apply"|"delete"|"create")(closure: Scope.Root ?=> Unit) = 
    val x = obj:
      closure.apply
    // 对x做点事情
    println(x)
object SampleTest:
  @main def main: Unit = 
    import kubernetes.{*,given}
    context("aaa","apply"):
      Namespace(name := "bbb"):
        Deployment(name := "ccc"):
          labels ++= Map(
            "aa" -> "bb"
          )
          labels += "1" -> "2"
      Namespace(name := "ccc"):
        Deployment(name := "ddd"):
          labels ++= Seq(
            "aa" -> "bb"
          )
          spec({}):
            template({}):
              labels ++= Seq(
                "app" -> ""
              )