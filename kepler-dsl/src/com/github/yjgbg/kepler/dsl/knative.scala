package com.github.yjgbg.kepler.dsl

object knative:
  import core.*
  import kubernetes.base.*
  export kn.given
  object kn:
    val Service:"Service" = compiletime.constValue
    given MultiNodeKey[Service.type,Root >> Namespace.type,Service.type] = Key.multiNodeKey
    val name:"name" = compiletime.constValue
    given [A <: _ >> Service.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey