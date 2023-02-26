package com.github.yjgbg.adserving

import zhttp.http.*
import zhttp.service.Server
import zio.*
import java.lang.management.ManagementFactory

import com.github.yjgbg.adserving.provider
object web extends ZIOAppDefault {
  override def run = for {
    _ <- ZIO.unit
    mxBean = ManagementFactory.getRuntimeMXBean()
    _ <- Console.printLine(s"Java VM (${mxBean.getVmName()} ${mxBean.getVmVersion()}) started on ${mxBean.getUptime() / 1000.0} s")
    x <- Server.start(8090, app)
  } yield x
  val app: HttpApp[Any, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "bid" => for {
      chunk <- req.body
      adxCode = req.url.queryParams("adx").head
      nid = req.url.queryParams("nid").head
      _ <- ZIO.debug(s"adxCode:${adxCode},nid:${nid}")
      adxAdaptor = AdxAdaptor(adxCode)
      (limit,state,evaluatorSeq) <- adxAdaptor.evaluator(nid,chunk)
      searchResult = evaluatorSeq.map(
        (zoneKey,evaluator) => provider.store.search(zoneKey ,limit,evaluator)
      )
    } yield adxAdaptor.handler(state,searchResult)
  }
}
