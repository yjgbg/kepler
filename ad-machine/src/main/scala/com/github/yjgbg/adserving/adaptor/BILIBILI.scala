package com.github.yjgbg.adserving
package adaptor

import com.github.yjgbg.adserving.engine.Evaluator

import com.github.yjgbg.adserving.biz.Targeting

import zio.UIO
import com.github.yjgbg.adserving.biz.Creative
import com.github.yjgbg.adserving.engine.Item
import zhttp.http.Response
import com.github.yjgbg.adserving.biz.Targeting.IdType
import com.github.yjgbg.adserving.engine.Evaludator

object BILIBILI extends adxAdaptor("BILIBILI") with utils:
  override type State = Request

  override def evaluator(byteArray: Array[Byte]): UIO[(Limit, State, RequestId, Seq[Evaluator[Targeting]])] = for {
    _ <- zio.ZIO.unit
    request = objectMapper.readValue(byteArray, classOf[Request]).nn
  } yield (10,request,request.id.toString(),Seq(Evaludator { 
    case _ => true
  }))
  override def handler(state: State, seq: Seq[Seq[Item[Creative]]]): Response = {
    val response = BilibiliResponse(state.id, seq.map { _.map { _.data } })
    Response(
      status = zhttp.http.Status.Ok,
      headers = zhttp.http.Headers("ContentType", "application/json"),
      body = zhttp.http.Body.fromString(objectMapper.writeValueAsString(response).nn)
    )
  }

  case class Request(id: Long, imps: Seq[Imp])
  case class Imp()
  case class BilibiliResponse(id: Long, seq: Seq[Seq[Creative]])
