package com.github.yjgbg.adserving

trait utils:
  import biz.Targeting.IdType
  def idType(
    imei:String|Null,
    oaid:String|Null,
    idfa:String|Null,
    cookie:String|Null,
    ):IdType = 
      if (imei != null) IdType.IMEI
      else if (oaid != null) IdType.OAID
      else if (idfa != null) IdType.IDFA
      else if (cookie!=null) IdType.COOKIE
      else IdType.UNKNOWN
  extension [A](a:A) 
    def |>[B](f: A => B): B|Null = if a == null then null else f(a)
object utils extends utils