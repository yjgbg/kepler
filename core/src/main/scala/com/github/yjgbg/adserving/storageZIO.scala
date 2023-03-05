package com.github.yjgbg.adserving

import engine.*
import biz.Targeting.*
import biz.Creative

import reflect.Selectable.reflectiveSelectable
extension [A:Ordering](enumCompanion:{def values:Array[A]}) 
  inline def any:DNF[A]|Null = enumCompanion.values.map{a => !(! a)}
    .reduceLeftOption((a,b) => a || b).orNull
  inline def all:DNF[A]|Null = enumCompanion.values.map{a => !(! a)}
    .reduceLeftOption((a,b) => a && b).orNull
val storageZIO:zio.Task[Searchine[Creative,biz.Targeting,Ready.Yes.type]] = zio.ZIO.succeed{
  Searchine[Creative,biz.Targeting]
  // 分区为adxId拼租户id,可被search到1次，定向为5G网络，投放元素为创意10L
  .load(
    "IQIYI",1L,
    Network.Network5G,
    Creative(10L,10L,20L,100))
  .load(
    "IQIYI",2L,
    Gender.Male && AgeBetween(18,23) || Gender.Female && AgeBetween(23,30),
    Creative(20L,20L,20L,100))
  .load(
    "IQIYI",3L,
    Gender.Male && AgeBetween(18,23),
    Creative(20L,20L,30L,100))
  .load(
    "IQIYI",4L,
    OS.IOS.any.nn || OS.ANDROID.ANDROID6,
    Creative(20L,20L,30L,100))
  .load("BILIBILI-123",4L,AdxCode("BILIBILI"),Creative(100L,100L,100L,100))
  .ready
}