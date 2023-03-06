package com.github.yjgbg.adserving

import zhttp.http.Response

trait adxAdaptor(val adxCode:String):
  type Limit = Int
  type RequestId = String
  type State
  def evaluator(byteArray:Array[Byte])
    :zio.UIO[(Limit,State,RequestId,Seq[engine.Evaluator[biz.Targeting]])]
  def handler(state:State,seq:Seq[Seq[engine.Item[biz.Creative]]]):zhttp.http.Response

object adxAdaptor:
  private var all:Seq[adxAdaptor] = Vector(
    adaptor.IQIYI,adaptor.BILIBILI
  )
  def apply(adxCode:String):adxAdaptor = 
    all.find{_.adxCode == adxCode}.getOrElse(fallback)
  val fallback:adxAdaptor = new adxAdaptor("fallback"):
    override type State = Unit
    override def evaluator(chunk: Array[Byte]) = 
      zio.ZIO.succeed((0,(),"fallback", Seq(engine.Evaludator{ _ => false})))
    override def handler(state:State,seq: Seq[Seq[engine.Item[biz.Creative]]]) = 
      zhttp.http.Response(zhttp.http.Status.NoContent)

