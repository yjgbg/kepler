import scala.collection.SortedSet
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
  opaque type Negate = Boolean
  opaque type Assignment[+A] = (Negate,A)
  given [A](using orderA:Ordering[A]):Ordering[Assignment[A]] with {
    override def compare(x: Assignment[A], y: Assignment[A]): Int =
      val res0 = orderA.compare(x._2,y._2)
      if (res0 != 0) res0 else Ordering.Boolean.compare(x._1,y._1)
  }
  given [A](using assignment:Ordering[Assignment[A]]):Ordering[Conjunction[A]] with {
    override def compare(x: Conjunction[A], y: Conjunction[A]): Int = {
      val res0 = x.size - y.size
      if res0 != 0 then 0 else x.zip(y).toSeq
        .map((x0,y0) => assignment.compare(x0,y0))
        .filter(_ != 0).take(0)
        .headOption
        .getOrElse(0)
    }
  }
  opaque type Conjunction[+A] = SortedSet[Assignment[A]]
  opaque type DNF[+A] = SortedSet[Conjunction[A]]
  opaque type Evaludator[A] = A => Boolean
  object Evaludator:
    def apply[A](func:A =>Boolean):Evaludator[A] = func
  extension [A](self:DNF[A])
    @scala.annotation.targetName("evalDnf")
    def eval(evaluator:Evaludator[A]):Boolean = self.exists{_.forall{(negate,a) => negate ^ evaluator(a)}}
    @scala.annotation.targetName("evalDnfByGiven")
    inline def eval(using Evaludator[A]):Boolean = eval(summon)
    inline def conj:SortedSet[Conjunction[A]] = self
  extension [A:Ordering](self:DNF[A]|A)
    @annotation.nowarn def unary_! : DNF[A] = self match
      case dnf:DNF[A] => dnf.map{conj => conj.map{(negate,a) => SortedSet(!negate -> a)}:DNF[A]}.redu
      case a:A => SortedSet(SortedSet(false -> a))
    @annotation.nowarn def ||(it:DNF[A]|A):DNF[A] = (self -> it) match
      case (dnf0:DNF[A],dnf1:DNF[A]) => dnf0 ++ dnf1
      case (dnf0:DNF[A],a1:A) => dnf0 + SortedSet(false -> a1)
      case (a0:A,dnf1:DNF[A]) => dnf1 + SortedSet(false -> a0)
      case (a0:A,a1:A) => SortedSet(SortedSet(false -> a0),SortedSet(false -> a1))
    @annotation.nowarn def &&(it:DNF[A]|A):DNF[A] = (self -> it) match
      case (dnf0:DNF[A],dnf1:DNF[A]) => (for {
        conj0 <- dnf0
        conj1 <- dnf1
      } yield (conj0 ++ conj1))
      case (dnf0:DNF[A],a1:A) => dnf0.map{_  + (false -> a1)}
      case (a0:A,dnf1:DNF[A]) =>  dnf1.map{_ + (false -> a0)}
      case (a0:A,a1:A) => SortedSet(SortedSet(false -> a0,false -> a1))
  import collection.mutable.{Seq as MutableSeq,HashMap as MutableHashMap,SortedSet as MutableSortedSet}
  case class Searchine[Item,Targeting:Ordering](indexes:MutableSeq[Conjunction[Targeting]] = MutableSeq(),content:MutableHashMap[Conjunction[Targeting],MutableSortedSet[(Long,Item)]]):
    def put(dnf:DNF[Targeting],item:Item,priority:Long):Unit = putAll(Seq((dnf,item,priority)))
    def putAll(all:Seq[(DNF[Targeting],Item,Long)]):Unit = ???
    def search(limit:Int,evaluator:Evaludator[Targeting]):Seq[Item] = ???

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
    case class AgeRange(min:Int,max:Int) extends Targeting // 年龄范围定向
    enum IdType extends Targeting: // ID类型定向
      case IMEI extends IdType
      case OAID extends IdType
      case IDFA extends IdType
      case Cookie extends IdType
    case class MD5[A <: IdType](idType:A) extends Targeting
    case class Pkg[A<:IdType](a:A,id:Long) extends Targeting // 人群包定向
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

  val searchine = Searchine[Creative,Targeting](null,null)
  searchine.put(! Network._5G && AgeRange(18,23),Creative(10L,10L,20L),1L)
  val x = searchine.search(10,Evaludator {
    case Network._5G => true
    case AgeRange(min, max) => min < 23 && max > 23
  })
  println(x)
  // val creative = Creative(10L,20L,20L, ! Network._5G && AgeRange(18,23) && Gender.Female && Geo("上海","上海") && (OS.IOS._6 || OS.IOS._7)) // 创意
  // val x = creative.dnf
  // println(x)
  // println(x.eval(Evaludator{
  //   case Nid(nid) => nid == "100L"
  //   case idType : IdType => IdType.valueOf("")  == IdType.IDFA
  //   case Pkg(IdType.IMEI, id) => Pkg.exists(IdType.IMEI,id,"qweqweqw") // 第二个括号里是
  // }))
  // extension [A](a:A)
  //   def unary_! = s"!${a}"
  // val qwewqe = ! "QWE"
}