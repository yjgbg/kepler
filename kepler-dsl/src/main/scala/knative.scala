package com.github.yjgbg.kepler.dsl

object knative:
  export kubernetes.{*,given}
  export kn.given
  object kn:
    val Service:"kn.Service" = compiletime.constValue
    given MultiNodeKey[Service.type,Root >> Namespace.type,Service.type] = Key.multiNodeKey
    given knSvcName[A <: _ >> Service.type]:SingleValueKey[name.type,A,String] = Key.singleValueKey