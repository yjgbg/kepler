package com.github.yjgbg.adserving

import zhttp.http.*
import zhttp.service.Server
import zio.*
import java.lang.management.ManagementFactory

object web extends ZIOAppDefault {
  override def run = for {
    _ <- ZIO.unit
    _ = scribe.Logger.root.withMinimumLevel(scribe.Level.Debug).replace()
    mxBean = ManagementFactory.getRuntimeMXBean().nn
    _ = scribe.info(s"Java VM (${mxBean.getVmName()} ${mxBean.getVmVersion()}) started on ${mxBean.getUptime() / 1000.0} s")
    storage <- storageZIO
    _ = scribe.info(s"已加载所有创意到searchine搜索引擎")
    endless <- (Server(app(storage)).withPort(8090).make *> ZIO.never)
      .provideSomeLayer[Any](zhttp.service.EventLoopGroup.auto(0) ++ zhttp.service.server.ServerChannelFactory.auto ++ Scope.default)
    
  } yield endless
  def app(storage:engine.Searchine[biz.Creative,biz.Targeting,engine.Ready.Yes.type]) =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "bid" => for {
        chunk <- req.body
        adxCode = req.url.queryParams("adx").head
        nid = req.url.queryParams("nid").head
        _ = scribe.info(s"adxCode:${adxCode},nid:${nid}")
        adaptor = adxAdaptor(adxCode)
        (limit,state,id,evaluatorSeq) <- adaptor.evaluator(chunk)
        searchResult = evaluatorSeq.map(
          evaluator => storage.search(adxCode+"-"+nid,limit,evaluator)
        )
      } yield adaptor.handler(state,searchResult)
      case req @Method.GET -> !! / "hello" => for {
        _ <- ZIO.unit
        status = zhttp.http.Status.Ok
        data = zhttp.http.HttpData.fromString("HelloWorld")
      } yield Response(status = status,data = data)
      case req @ Method.GET -> !! / "statistic" => for {
        _ <- ZIO.unit
        status = zhttp.http.Status.Ok
        headers = zhttp.http.Headers("ContentType","application/json")
        jsonString = utils.objectMapper.writeValueAsString(storage.statistic).nn
        data = zhttp.http.HttpData.fromString(jsonString)
      } yield Response(status,headers,data)
  }
}
