package com.github.yjgbg.adserving

import zhttp.http.Response

trait Adaptor(val adxCode:String):
  type Limit = Int
  type ZoneKey = String
  type RequestId = String
  type State
  def evaluator(chunk:zio.Chunk[Byte])
    :zio.UIO[(Limit,State,RequestId,Seq[engine.Evaluator[biz.Targeting]])]
  def handler(state:State,seq:Seq[Seq[engine.Item[biz.Creative]]]):zhttp.http.Response

object Adaptor:
  private var all:Seq[Adaptor] = Vector(
    adaptor.IQIYI,adaptor.BILIBILI
  )
  def apply(adxCode:String):Adaptor = 
    all.find{_.adxCode == adxCode}.getOrElse(fallback)
  val fallback:Adaptor = new Adaptor("fallback"):
    override type State = Unit
    override def evaluator(chunk: zio.Chunk[Byte]) = 
      zio.ZIO.succeed((0,(),"fallback", Seq(engine.Evaludator{ _ => false})))
    override def handler(state:State,seq: Seq[Seq[engine.Item[biz.Creative]]]) = 
      zhttp.http.Response(zhttp.http.Status.NoContent)

