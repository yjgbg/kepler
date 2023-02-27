package com.github.yjgbg.adserving

import zhttp.http.Response

trait AdxAdaptor(val adxCode:String):
  type Limit = Int
  type ZoneKey = String
  type RequestId = String
  type State
  def evaluator(chunk:zio.Chunk[Byte]|Null)
    :zio.UIO[(Limit,State,RequestId,Seq[engine.Evaluator[biz.Targeting]])]
  def handler(state:State,seq:Seq[Seq[engine.Item[biz.Creative]]]):zhttp.http.Response

object AdxAdaptor:
  private var all:Seq[AdxAdaptor] = Vector(
    adaptor.IQIYI
  )
  def apply(adxCode:String):AdxAdaptor = 
    all.find{_.adxCode == adxCode}.getOrElse(fallback)
  val fallback:AdxAdaptor = new AdxAdaptor("fallback"):
    override type State = Unit
    override def evaluator(chunk: zio.Chunk[Byte]|Null) = 
      zio.ZIO.succeed((0,(),"fallback", Seq(engine.Evaludator{ _ => false})))
    override def handler(state:State,seq: Seq[Seq[engine.Item[biz.Creative]]]) = 
      zhttp.http.Response(zhttp.http.Status.NoContent)

