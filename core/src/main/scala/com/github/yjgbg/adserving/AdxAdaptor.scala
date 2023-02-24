package com.github.yjgbg.adserving

import zhttp.http.Response


trait AdxAdaptor(val adxCode:String):
  def evaluator(nid:String,chunk:zio.Chunk[Byte]):core.Evaluator[biz.Targeting,zio.Task]
  def handler(seq:Seq[core.Item[biz.Creative]]):Response
  def limit:Int

object AdxAdaptor:
  private lazy val all:Seq[AdxAdaptor] = Seq(
    adaptor.IQIYI
  )
  def apply(adxCode:String):AdxAdaptor = 
    all.find{_.adxCode == adxCode}.getOrElse(fallback)
  val fallback:AdxAdaptor = new AdxAdaptor("fallback"):
    override def evaluator(nid: String, chunk: zio.Chunk[Byte]): core.Evaluator[biz.Targeting, zio.Task] = 
      core.Evaludator{_ => zio.ZIO.succeed(false)}
    override def handler(seq: Seq[core.Item[biz.Creative]]): Response = 
      Response.ok
    override def limit: Int = 0

