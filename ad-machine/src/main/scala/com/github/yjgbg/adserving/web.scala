package com.github.yjgbg.adserving

import zhttp.http.*
import zhttp.service.Server
import zio.*
import java.lang.management.ManagementFactory

object web extends ZIOAppDefault with utils {
  case class AppConfig(pulsar:PulsarConfig,log:LogConfig)
  case class PulsarConfig(url:String = "pulsar://localhost:6650,localhost:6651,localhost:6652")
  case class LogConfig(handler:String)
  import zio.config.magnolia.{*,given}
  def run = (for {
    _ <- ZIO.unit
    appConfig <- ZIO.service[AppConfig] // autowired
    mxBean = ManagementFactory.getRuntimeMXBean().nn
    _ = scribe.info(s"Java VM (${mxBean.getVmName()} ${mxBean.getVmVersion()}) started on ${mxBean.getUptime() / 1000.0} s")
    storage <- storageZIO
    endless <- (Server(app(storage)).withPort(8090).make *> ZIO.never)
      .provideSomeLayer[Any](
        zhttp.service.EventLoopGroup.auto(0) 
        ++ zhttp.service.server.ServerChannelFactory.auto 
        ++ Scope.default)
  } yield endless)
  // @component
  .provideLayer(config.ZConfig.fromSystemEnv(zio.config.magnolia.descriptor[AppConfig],keyDelimiter = Some('.'),valueDelimiter = Some(',')))
  def app(storage:engine.Searchine[biz.Creative,biz.Targeting,engine.Ready.Yes.type]) =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "bid" => for {
        byteArray <- req.body.asArray
        adxCode = req.url.queryParams("adx").head
        nid = req.url.queryParams("nid").head
        _ = scribe.info(s"adxCode:${adxCode},nid:${nid}")
        adaptor = adxAdaptor(adxCode)
        (limit,state,id,evaluatorSeq) <- adaptor.evaluator(byteArray)
        searchResult = evaluatorSeq.map(
          evaluator => storage.search(id,adxCode+"-"+nid,limit,evaluator)
        )
      } yield adaptor.handler(state,searchResult)
      case req @ Method.GET -> !! / "statistic" => zio.ZIO.succeed(Response
        .text(utils.objectMapper.writeValueAsString(storage.statistic).nn)
        .withContentType("application/json"))
  }
}
