package kepler

object GitlabCi:
  export core.{*, given}
  import scala.jdk.CollectionConverters.{MapHasAsJava, BufferHasAsJava}
  private def processObj(obj: Any): Any = obj match
    case scope: Scope                                    => processObj(scope.value)
    case hashMap: scala.collection.mutable.HashMap[?, ?] => hashMap.mapValues { v => processObj(v) }.to(collection.mutable.HashMap).asJava
    case arrayBuffer: scala.collection.mutable.ArrayBuffer[?] => arrayBuffer.map(it => processObj(it)).asJava
    case other: Any                                           => other
  def GitlabCi(closure: Closure[Scope.Root]): Unit =
    import org.yaml.snakeyaml.{Yaml, DumperOptions}
    val root = obj(closure)
    val any = processObj(root)
    val dumperOptions = DumperOptions()
    dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
    val string = Yaml(dumperOptions).dump(any)
    os.write.over(os.pwd / "gitlab-ci.sc.yml", string)
  def job(using scope: Scope.Root)(name: String)(closure: Closure[Scope.Root >> "job"]): Unit =
    val svk: SingleNodeKey[name.type, Scope.Root, "job"] = name.asInstanceOf
    name.apply(using scope, Left(svk))(closure)
  val image: "image" = compiletime.constValue
  given SingleValueKey[image.type, Scope.Root >> "job", String] = Key.singleValueKey
  val script: "script" = compiletime.constValue
  given MultiValueKey[script.type, Scope.Root >> "job", String] = Key.multiValueKey
  val beforeScript: "before_script" = compiletime.constValue
  given MultiValueKey[beforeScript.type, Scope.Root >> "job", String] = Key.multiValueKey
  val retry: "retry" = compiletime.constValue
  given MultiValueKey[retry.type, Scope.Root >> "job", 0 | 1 | 2] = Key.multiValueKey
  val needs: "needs" = compiletime.constValue
  given MultiValueKey[needs.type, Scope.Root >> "job", String] = Key.multiValueKey
  val stage: "stage" = compiletime.constValue
  given MultiValueKey[stage.type, Scope.Root, String] = Key.multiValueKey
  given SingleValueKey[stage.type, Scope.Root >> "job", String] = Key.singleValueKey
  val artifacts: "artifacts" = compiletime.constValue
  given SingleNodeKey[artifacts.type, Scope.Root >> "job", artifacts.type] = Key.singleNodeKey
  val paths: "paths" = compiletime.constValue
  given MultiValueKey[paths.type, ? >> artifacts.type, String] = Key.multiValueKey
  val expireIn: "expire_in" = compiletime.constValue // seconds
  given SingleValueKey[expireIn.type, ? >> artifacts.type, Int] = Key.singleValueKey
  val cache: "cache" = compiletime.constValue
  given SingleNodeKey[cache.type, Scope.Root >> "job", cache.type] = Key.singleNodeKey
  given pathsInCache: MultiValueKey[paths.type, Scope.Root >> "job" >> cache.type, String] = Key.multiValueKey
  val key: "key" = compiletime.constValue
  given SingleValueKey[key.type, ? >> cache.type, String] = Key.singleValueKey
  val untracked: "untracked" = compiletime.constValue
  given SingleValueKey[untracked.type, ? >> cache.type, Boolean] = Key.singleValueKey

  import utils.*
  opaque type Event = Json
  opaque type Payload = Json
  private lazy val event:Json = sys.env("TRIGGER_PAYLOAD") |> {os.Path(_)} |> {os.read(_).asInstanceOf}
  lazy val payload:Json = sys.env("TRIGGER_PAYLOAD") 
    |> {os.Path(_)} 
    |> {os.read(_).asInstanceOf[Json]} 
    |> {_.path[String]("$.payload").asInstanceOf[Json]}

