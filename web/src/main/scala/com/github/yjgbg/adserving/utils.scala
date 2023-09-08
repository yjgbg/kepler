
trait utils:
  extension [A](a:A)
    def |>[B](f:A => B):B = f(a)
  extension [A](a: A|Null)
    def |>?[B](f: A => B|Null): B|Null = if a == null then null else f(a)
    def |?(other: => A):A = if a == null then other else a
  val objectMapper = com.fasterxml.jackson.databind.ObjectMapper() 
    |> {_.registerModule(com.fasterxml.jackson.module.scala.DefaultScalaModule).nn}
  import biz.Targeting.IdType
  final def idType(
    imei:String|Null,
    oaid:String|Null,
    idfa:String|Null,
    cookie:String|Null,
    ):IdType = 
      if (imei != null) IdType.IMEI
      else if (oaid != null) IdType.OAID
      else if (idfa != null) IdType.IDFA
      else if (cookie!=null) IdType.COOKIE
      else IdType.UNKNOWN
object utils extends utils