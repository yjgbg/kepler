package com.github.yjgbg.adserving

import engine.*
import biz.*
object provider:
  import Targeting.*
  lazy val store:ZonedSearchine[Creative, Targeting, Ready.Yes.type] = engine.Searchine
    .zoned[Creative,Targeting]
    .load("IQIYI",Network.Network5G,Creative(10L,10L,20L,100))
    .load("IQIYI",Gender.Male && AgeBetween(18,23) || Gender.Female && AgeBetween(23,30),Creative(20L,20L,20L,100))
    .load("IQIYI",Gender.Male && AgeBetween(18,23) ,Creative(20L,20L,30L,100))
    .load("IQIYI",OS.IOS.IOS6 || OS.IOS.IOS7 || OS.IOS.IOS8 || OS.IOS.IOS9 || OS.IOS.IOS10 || OS.IOS.IOS11 || OS.IOS.IOS12 || OS.IOS.IOS13 || OS.IOS.IOS14 || OS.IOS.IOS15 || OS.IOS.IOS16,Creative(20L,20L,30L,100))
    .ready