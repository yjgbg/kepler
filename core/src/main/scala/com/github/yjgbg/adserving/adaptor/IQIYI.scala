package com.github.yjgbg.adserving
package adaptor

import biz.Targeting.*
import zio.ZIO
import zhttp.http.Response
import zio.Chunk
import zhttp.http.Request

object IQIYI extends AdxAdaptor("IQIYI") with utils:
  override def evaluator(nid:String,chunk:zio.Chunk[Byte]): core.Evaluator[biz.Targeting, zio.Task] = 
    val request = iqiyi.Request.BidRequest.parseFrom(chunk.toArray)
    val lens = scalapb.lenses.Lens.unit[iqiyi.Request.BidRequest]
    val imei = lens.device.imei.get(request)
    val oaid = lens.device.oaid.get(request)
    val caid = lens.device.caidInfo.caid.get(request)
      .max(using Ordering.by(_.version)).caid.orNull
    val idType = this.idType(imei,oaid)
    val osUpperCase = lens.device.os.get(request) ?? {_.toUpperCase}
    val osv = lens.device.osVersion.get(request)
    core.Evaludator {
      case AdxCode(code) => ZIO.succeed(code == adxCode)
      case network: Network => ZIO.succeed(network == Network._5G)
      case os: OS => ZIO.succeed(os.toString() == osUpperCase)
      case AgeBetween(min, max) => ZIO.succeed(min < 23 && max > 23)
      case gender: Gender => ZIO.succeed(gender == Gender.Male)
      case idType0:IdType => ZIO.succeed(idType == idType0)
    }
  override def limit:Int = 10
  override def handler(seq: Seq[core.Item[biz.Creative]]): Response = 
    val x = iqiyi.Response.BidResponse.defaultInstance
    Response(
      headers = zhttp.http.Headers(
        "ContentType" -> "application/protobuf"
      ),
      data = zhttp.http.HttpData.fromChunk(Chunk.fromArray(x.toByteArray))
    )
