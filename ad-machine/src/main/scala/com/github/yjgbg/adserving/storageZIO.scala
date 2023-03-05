package com.github.yjgbg.adserving

import engine.*
import biz.Targeting.*
import biz.Creative

val storageZIO = zio.ZIO.succeed{
  Searchine[Creative,biz.Targeting]
  .load(
    "IQIYI",1L, // 分区，可被search的次数
    Network.Network5G, // 定向条件
    Creative(10L,10L,20L,100)) // 数据对象
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
    DNF.any(OS.IOS.values ++ OS.ANDROID.values).nn,
    Creative(20L,20L,30L,100))
  .load("BILIBILI-123",4L,AdxCode("BILIBILI"),Creative(100L,100L,100L,100))
  .ready
}