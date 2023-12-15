package com.github.yjgbg.kepler.dsl

object server:
  export core.{*,given}
  def keplerServer(closure:Closure[Root]) = 
    val x = obj(closure)
    println(x)
  val http:"http" = compiletime.constValue
  given MultiNodeKey[http.type,Root,http.type] = Key.multiNodeKey
  val listen:"address" = compiletime.constValue
  given [A <: _ >> http.type]:SingleValueKey[listen.type,A,String] = Key.singleValueKey 
  val endpoint: "endpoint" = compiletime.constValue
  given [A <: _ >> http.type]:MultiNodeKey[endpoint.type,A,endpoint.type] = Key.multiNodeKey
  val path: "path" = compiletime.constValue
  given [A <: _ >> endpoint.type]:SingleValueKey[path.type,A,String] = Key.singleValueKey
  val method:"method" = compiletime.constValue
  given [A <: _ >> endpoint.type]:SingleValueKey[method.type,A,"POST"|"GET"] = Key.singleValueKey
  val action: "action" = compiletime.constValue
  given [A <: _ >> endpoint.type]:SingleValueKey[action.type,A,String => String] = Key.singleValueKey
  val middleware:"middleware" = compiletime.constValue
  given [A <: _ >> http.type | _ >> endpoint.type]:MultiValueKey[middleware.type,A,String => String] = Key.multiValueKey
object example:
  import server.{*,given}
  keplerServer:
    http(listen := "0.0.0.0:8080"):
      middleware += {req => println(req);req} //记录日志
      endpoint(path := "/api/users/:userId",method := "GET"):
        action := {a => a}
