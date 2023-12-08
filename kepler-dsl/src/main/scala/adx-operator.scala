package com.github.yjgbg.kepler.dsl

object adxOperator:
  export core.{*,given}
  // def adx(name:String,code:String)(closure:Scope.Root ?=> Unit):Unit = ???
  val adx:"adx" = compiletime.constValue
  given MultiNodeKey[adx.type,Root,adx.type] = Key.multiNodeKey
  val webSite:"webSite" = compiletime.constValue
  given MultiNodeKey[webSite.type,_ >> adx.type,webSite.type] = Key.multiNodeKey
  val name:"name" = compiletime.constValue
  given [A <: 
    _ >> adx.type
    | _ >> webSite.type
    | _ >> channel.type
    ]:SingleValueKey[name.type,A,String] = Key.singleValueKey
  val code:"code" = compiletime.constValue
  given [A <: _ >> adx.type]:SingleValueKey[code.type,A,String] = Key.singleValueKey
  val channel:"channel" = compiletime.constValue
  given MultiNodeKey[channel.type,_ >> webSite.type,channel.type] = Key.multiNodeKey

object Sample:
  def main =
    import adxOperator.{*,given}
    obj:
      adx:
        name := "adxName"
        code := "code"
        webSite:
          name := "name"
          channel:
            name := "channelName"
            println()