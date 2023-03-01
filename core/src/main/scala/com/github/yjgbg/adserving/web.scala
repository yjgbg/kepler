package com.github.yjgbg.adserving

import zhttp.http.*
import zhttp.service.Server
import zio.*
import java.lang.management.ManagementFactory

import com.github.yjgbg.adserving.provider
object web extends ZIOAppDefault {
  override def run = for {
    _ <- ZIO.unit
    _ = scribe.Logger.root.withHandler()
    mxBean = ManagementFactory.getRuntimeMXBean().nn
    _ = scribe.info(s"Java VM (${mxBean.getVmName()} ${mxBean.getVmVersion()}) started on ${mxBean.getUptime() / 1000.0} s")
    x <- Server(app).withPort(8090).make
      .provideSomeLayer[Any](zhttp.service.EventLoopGroup.auto(0) ++ zhttp.service.server.ServerChannelFactory.auto ++ Scope.default)
      *> ZIO.never
  } yield x
  val app: HttpApp[Any, Throwable] = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "bid" => for {
      chunk <- req.body
      adxCode = req.url.queryParams("adx").head
      nid = req.url.queryParams("nid").head
      _ = scribe.info(s"adxCode:${adxCode},nid:${nid}")
      adxAdaptor = AdxAdaptor(adxCode)
      (limit,state,id,evaluatorSeq) <- adxAdaptor.evaluator(chunk)
      searchResult = evaluatorSeq.map(
        evaluator => provider.store.search(adxCode+"-"+nid,limit,evaluator)
      )
    } yield adxAdaptor.handler(state,searchResult)
  }
}
