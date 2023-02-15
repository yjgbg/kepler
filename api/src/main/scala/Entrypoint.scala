import zhttp.http._
import zhttp.service.Server
import zio._

object ZHttpExample extends ZIOAppDefault {

  val app: HttpApp[Any, Throwable] =
    Http.collectZIO[Request] {
      case req @ Method.GET -> !! / "bid" / adxCode / nid => for {
        _ <- Console.printLine(nid)
        // body <- req.body // 拿取请求体
        adxAdaptor = AdxAdaptor.adxAdaptorMap.get(adxCode).orNull // 根据adxCode查询适配器
        assignmentEvaluator = adxAdaptor.nn.plan(nid,null) // 根据适配器创建查询计划
        materialSeq = Material.search(assignmentEvaluator) // 根据查询计划搜索到素材
      } yield adxAdaptor.handle(materialSeq) // 使用适配器将素材转换为响应
    }

  override def run =
    Server.start(8090, app)
}
type AssignmentEvaluator = (Assignment => Boolean)

case class Material(dnf:DNF)
object Material:
  def search(evaluator:AssignmentEvaluator):Seq[Material] = {
    val conjSet = store.keySet.filter{conj => conj.forall(evaluator.apply)} // EVAL每一个assignment，算出Conjunction的真值
    conjSet.toSeq.flatMap(store(_)).filter(_ != null)// 以其中为真的conj作为键去搜索
  }
  type Hash = String
  var store:Map[Conjunction,Seq[Material]] = Map(
    Seq(Assignment.Tenant("123")) -> Seq(Material(Seq(Seq(Assignment.Tenant("123")))))
  )
  // def load(material:Material):Unit = ???
enum Assignment:
  case Tenant(nid:String) extends Assignment
type Conjunction = Seq[Assignment]
type DNF = Seq[Conjunction]
object DNF:
  def tenant(nid:String):DNF = Seq(Seq(Assignment.Tenant(nid)))
extension (self:DNF)
  def &&(dnf:DNF):DNF = for {
    con0 <- self
    con1 <- dnf
  } yield con0 :++ con1
  def ||(dnf:DNF):DNF = self :++ dnf
trait AdxAdaptor:
  def plan(nid:String,byteBuffer:Chunk[Byte]):AssignmentEvaluator
  def handle(materialSeq:Seq[Material]):Response = Response.text(materialSeq.toString())
object AdxAdaptor:
  val adxAdaptorMap:Map[String,AdxAdaptor] = Map(
    "iqiyi" -> IQIYIADX
  )
  def apply(code:String):AdxAdaptor|Null = adxAdaptorMap.get(code).orNull
object IQIYIADX extends AdxAdaptor {
  override def plan(nid:String,bodyBuffer:Chunk[Byte]):AssignmentEvaluator = 
    case Assignment.Tenant(value) => value == nid
  
  override def handle(materialSeq: Seq[Material]): Response = {
    Response.text(materialSeq.toString)
  }
}