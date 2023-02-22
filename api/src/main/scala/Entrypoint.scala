import scala.collection.SortedSet
import scala.collection.SortedMap
// import zhttp.http._
// import zhttp.service.Server
// import zio._

// object ZHttpExample extends ZIOAppDefault {
//   override def run = Server.start(8090, app)
//   val app: HttpApp[Any, Throwable] =
//     Http.collectZIO[Request] {
//       case req @ Method.GET -> !! / "bid" / adxCode / nid => for {
//         _ <- Console.printLine(nid)
//         // body <- req.body // 拿取请求体
//         adxAdaptor = AdxAdaptor.adxAdaptorMap.get(adxCode).orNull // 根据adxCode查询适配器
//         assignmentEvaluator = adxAdaptor.nn.plan(nid,null) // 根据适配器创建查询计划
//         materialSeq = Material.search(assignmentEvaluator) // 根据查询计划搜索到素材
//       } yield adxAdaptor.handle(materialSeq) // 使用适配器将素材转换为响应
//     }
// }

object DnfDsl:
  case class Assignment[A](flag:Boolean,it:A)
  opaque type Conjunction[A] = Seq[Assignment[A]]
  opaque type DNF[A] = Seq[Conjunction[A]]
  opaque type Evaluator[A] = A => Boolean
  given [A:Ordering]:Ordering[Assignment[A]] =  Ordering.by[Assignment[A],A](_.it).orElseBy(_.flag)
  given [A:Ordering]:Ordering[Conjunction[A]] = Ordering[Conjunction[A]]{(a,b) =>
    if(a==b) 0 else {
      val res0 = Ordering[Int].compare(a.size,b.size)
      if(res0!=0) res0 else {
        val res1 = Ordering[Assignment[A]].compare(a.head,b.head)
        if(res1!=0) res1 else Ordering[Conjunction[A]].compare(a.tail,b.tail)
      }
    }
  }
  object Evaludator:
    def apply[A](func:A =>Boolean):Evaluator[A] = func
  extension [A](self:DNF[A])
    inline def conj:Seq[Conjunction[A]] = self
  extension [A:Ordering](self:DNF[A]|A)
    @annotation.nowarn def unary_! : DNF[A] = self match
      case dnf:DNF[A] => dnf.map{conj => conj.toSeq.map{case Assignment(flag,it) => Seq(Assignment(!flag,it))}}.reduce(_ && _)
      case a:A => Seq(Seq(Assignment(false,a)))
    @annotation.nowarn def ||(it:DNF[A]|A):DNF[A] = (self -> it) match
      case (dnf0:DNF[A],dnf1:DNF[A]) => dnf0 ++ dnf1
      case (dnf0:DNF[A],a1:A) => dnf0 :+ Seq(Assignment(true,a1))
      case (a0:A,dnf1:DNF[A]) => Seq(Assignment(true,a0)) +: dnf1
      case (a0:A,a1:A) => Seq(Assignment(true,a0)) +: Seq(Assignment(true,a1)) +: Nil
    @annotation.nowarn def &&(it:DNF[A]|A):DNF[A] = (self -> it) match
      case (dnf0:DNF[A],dnf1:DNF[A]) => (for {
        conj0 <- dnf0
        conj1 <- dnf1
      } yield (conj0 ++ conj1))
      case (dnf0:DNF[A],a1:A) => dnf0.map{_  :+ (Assignment(true,a1))}
      case (a0:A,dnf1:DNF[A]) =>  dnf1.map{(Assignment(true,a0)) +: _}
      case (a0:A,a1:A) => Seq(Seq(Assignment(true,a0),Assignment(true,a1)))
  import collection.mutable
  trait Searchine[E,T:Ordering]:
    def put(dnf:DNF[T],e:E,priority:Long):Unit
    def search(limit:Int,evaluator:Evaluator[T]):Seq[(Long,E)]
  object Searchine:
    def apply[E,T:Ordering]:Searchine[E,T] = new Searchine:
      private val content = mutable.HashMap[Conjunction[T],mutable.SortedSet[(Long,E)]]()
      lazy val conjunctionSet = content.keySet
      lazy val tSet = conjunctionSet.flatMap(_.map(_.it))
      override def put(dnf: DNF[T], e: E, priority: Long): Unit = {
        dnf.foreach{conj => 
          content.getOrElseUpdate(conj.sorted,mutable.SortedSet()(using Ordering.by(_._1))) += priority -> e
        }
      }
      override def search(limit: Int, evaluator: Evaluator[T]): Seq[(Long, E)] = {
        val res = tSet.map(it => it -> evaluator(it)).toMap
        conjunctionSet
        .filter{conj => conj.forall{ass => !ass.flag ^ res(ass.it)}}
        .flatMap{content.getOrElse(_,mutable.SortedMap[Long,E]())}
        .toSeq
        .sortBy((priority,e) => -priority)
        .take(limit)
      }
      override def toString(): String = s"Searchine(\n\tcontent=$content\n\tconjunctionSet=$conjunctionSet,\n\ttSet=$tSet)"

@main def run = {
  sealed trait Resource:
    val id:Long
  case class MaterialTemplate(override val id:Long,val adxId:Long) extends Resource // 素材模板
  case class Material(override val id:Long,val templateId:Long) extends Resource// 素材
  case class Creative(override val id:Long,orderItemId:Long,materialId:Long) extends Resource // 创意
  case class OrderItem(override val id:Long,orderId:Long) extends Resource // 订单项
  case class Order(override val id:Long,advertiserId:Long) extends Resource // 订单
  case class AdUnit(override val id:Long,adxId:Long) extends Resource // 广告位
  case class AdUnitMaterialTemplateCompatible(override val id:Long,adUnitId:Long,materialTemplateId:Long) extends Resource
  case class Advertiser(override val id:Long,name:String) extends Resource // 广告主
  case class Adx(override val id:Long,name:String,code:String,eval:Targeting => Unit) extends Resource
  sealed trait Targeting
  object Targeting:
    case class Nid(nid:String) extends Targeting
    enum Gender extends Targeting:
      case Male extends Gender
      case Female extends Gender
    enum Network extends Targeting:
      case WIFI extends Network
      case _3G extends Network
      case _4G extends Network
      case _5G extends Network
    case class Geo(province:String,city:String) extends Targeting // 地域定向
    case class AgeBetween(min:Int,max:Int) extends Targeting // 年龄范围定向
    enum IdType extends Targeting: // ID类型定向
      case IMEI extends IdType
      case OAID extends IdType
      case IDFA extends IdType
      case Cookie extends IdType
    case class MD5[A <: IdType](idType:A) extends Targeting
    case class Pkg[A<:IdType](idType:A,pkgId:Long) extends Targeting // 人群包定向
    sealed trait OS extends Targeting
    object OS:
      enum Android extends OS:
        case _6 extends Android
        case _7 extends Android
        case _8 extends Android
        case _9 extends Android
        case _10 extends Android
        case _11 extends Android
        case _12 extends Android
        case _13 extends Android
      enum IOS extends OS:
        case _6 extends IOS
        case _7 extends IOS
        case _8 extends IOS
        case _9 extends IOS
        case _10 extends IOS
        case _11 extends IOS
        case _12 extends IOS
        case _13 extends IOS
        case _14 extends IOS
        case _15 extends IOS
        case _16 extends IOS
      case object Windows extends OS
      case object WindowsPhone extends OS
  import DnfDsl.*
  import Targeting.*
  given [A <: Targeting]:Ordering[A] = Ordering.by(_.toString())
  val searchine = Searchine[Creative,Targeting]
  searchine.put(Network._5G || Network.WIFI,Creative(10L,10L,20L),2L)
  searchine.put(Gender.Male && AgeBetween(18,23) || Gender.Female && AgeBetween(23,30) ,Creative(20L,20L,20L),3L)
  searchine.put(Gender.Male && AgeBetween(18,23) || Gender.Female && AgeBetween(23,30) ,Creative(20L,20L,30L),4L)
  println(searchine)
  val x = searchine.search(1,Evaludator {
    case network:Network => network.ordinal < Network._5G.ordinal
    case AgeBetween(min, max) => min < 22 && max > 22
    case gender:Gender => gender == Gender.Male
  })
  println(x)
}