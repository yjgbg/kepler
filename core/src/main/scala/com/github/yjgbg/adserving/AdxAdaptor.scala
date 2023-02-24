package com.github.yjgbg.adserving

import zhttp.http.Response


trait AdxAdaptor(val adxCode:String):
  def evaluator(nid:String,chunk:zio.Chunk[Byte]):core.Evaluator[biz.Targeting,zio.Task]
  def handler(seq:Seq[core.Item[biz.Creative]]):Response
  def limit:Int

object AdxAdaptor:
  val all:Seq[AdxAdaptor] = Seq(iqiyi)
  def apply(adxCode:String):AdxAdaptor = all.find(_.adxCode == adxCode).getOrElse(fallback)
  val fallback:AdxAdaptor = new AdxAdaptor("fallback"):
    override def evaluator(nid: String, chunk: zio.Chunk[Byte]): core.Evaluator[biz.Targeting, zio.Task] = 
      core.Evaludator{_ => zio.ZIO.succeed(false)}
    override def handler(seq: Seq[core.Item[biz.Creative]]): Response = 
      Response.ok
    override def limit: Int = 0
  val iqiyi:AdxAdaptor = new AdxAdaptor("IQIYI"):
    import biz.Targeting.*
    import zio.ZIO
    override def evaluator(nid:String,chunk:zio.Chunk[Byte]): core.Evaluator[biz.Targeting, zio.Task] = 
      core.Evaludator {
        case AdxCode(code) => ZIO.succeed(code == adxCode)
        case idType:IdType => ZIO.succeed(true)
        case network: Network => ZIO.succeed(network == Network._5G)
        case AgeBetween(min, max) => ZIO.succeed(min < 23 && max > 23)
        case gender: Gender => ZIO.succeed(gender == Gender.Male)
      }
    override def limit:Int = 10
    override def handler(seq: Seq[core.Item[biz.Creative]]): Response = 
      Response.text(s"$adxCode:${seq.mkString(",")}")
