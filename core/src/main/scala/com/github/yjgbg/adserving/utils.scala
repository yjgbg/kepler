package com.github.yjgbg.adserving

trait utils:
  def idType(
    imei:String|Null,
    oaid:String|Null
    ):biz.Targeting.IdType = 
    if (imei != null) biz.Targeting.IdType.IMEI
    else if (oaid != null) biz.Targeting.IdType.OAID
    else biz.Targeting.IdType.UNKNOWN
  extension [A](a:A) def ??[B](f:A => B):B|Null = 
    if a == null then null else f(a)
object utils extends utils