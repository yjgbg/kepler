package com.github.yjgbg.adserving
package adaptor

import biz.Targeting.*
import zio.{ZIO,Chunk}
import zhttp.http.{Request,Response}

object IQIYI extends AdxAdaptor("IQIYI") with utils:
  override type State = iqiyi.Request.BidRequest
  override def evaluator(chunk:zio.Chunk[Byte]) = for {
    _ <- ZIO.unit
    request = iqiyi.Request.BidRequest.parseFrom(chunk.toArray)
    lens = scalapb.lenses.Lens.unit[iqiyi.Request.BidRequest]
    imei = lens.device.imei.get(request)
    oaid = lens.device.oaid.get(request)
    caidInfo = lens.device.caidInfo.caid.get(request)
    idType = utils.idType(imei,oaid,null,null)
    osUpperCase = lens.device.os.get(request) |> {_.toUpperCase}
    osv = lens.device.osVersion.get(request)
  } yield (10,request,request.id.orNull.nn,for (imp <- request.imp) yield engine.Evaludator {
    case AdxCode(code) => code == adxCode
    case network: Network => network == Network.Network5G
    case os: OS => os.toString() == osUpperCase
    case AgeBetween(min, max) => min < 23 && max > 23
    case gender: Gender => gender == Gender.Male
    case idType0:IdType => idType == idType0
    case _ => null
  })
  override def handler(req:State,seq: Seq[Seq[engine.Item[biz.Creative]]]) = 
    val lens = scalapb.lenses.Lens.unit[iqiyi.Response.BidResponse]
    val bidResponse = iqiyi.Response.BidResponse(
      id = req.id.orNull.nn,
      seatbid = for ((imp,sr) <- req.imp.zip(seq)) yield iqiyi.Response.Seatbid(
        bid = sr.map{item => iqiyi.Response.Bid(
          id = req.id.orNull.nn,
          impid =  imp.id.orNull.nn,
          price = item.data.price.toInt,
          adm = "adm",
          crid = item.data.id.toString
        )}
      )
    )
    Response(
      headers = zhttp.http.Headers(
        "ContentType" -> "application/protobuf"
      ),
      data = zhttp.http.HttpData.fromChunk(Chunk.fromArray(bidResponse.toByteArray))
    )
