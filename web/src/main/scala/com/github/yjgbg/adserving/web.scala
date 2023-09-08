
import zhttp.http.*
import zhttp.service.Server
import zio.*
import java.lang.management.ManagementFactory
import com.github.yjgbg.adserving.biz.Creative

object web extends ZIOAppDefault with utils {
  case class AppConfig(pulsar:PulsarConfig,log:LogConfig)
  case class PulsarConfig(url:String = "pulsar://localhost:6650,localhost:6651,localhost:6652")
  case class LogConfig(handler:String)
  import zio.config.magnolia.{*,given}
  def run = (for {
    _ <- ZIO.unit
    mxBean = ManagementFactory.getRuntimeMXBean().nn
    _ = scribe.info(s"Java VM (${mxBean.getVmName()} ${mxBean.getVmVersion()}) started on ${mxBean.getUptime() / 1000.0} s")
    storage <- storageZIO
    endless <- (Server(app(storage)).withPort(8090).make *> ZIO.never)
      .provideSomeLayer[Any](
        zhttp.service.EventLoopGroup.auto(0) 
        ++ zhttp.service.server.ServerChannelFactory.auto 
        ++ Scope.default
        ++ ZLayer(ZIO.succeed(AppConfig(PulsarConfig(),LogConfig("")))))
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
        searchResult:Seq[ZIO[AppConfig,Nothing,Seq[engine.Item[Creative]]]] = evaluatorSeq.map(
          evaluator => storage.search(id,adxCode+"-"+nid,limit,evaluator)
        )
        x <- zio.ZIO.collect(searchResult){x => x}
      } yield adaptor.handler(state,x)
      case req @ Method.GET -> !! / "statistic" => zio.ZIO.succeed(Response
        .text(utils.objectMapper.writeValueAsString(storage.statistic).nn)
        .withContentType("application/json"))
  }
}
