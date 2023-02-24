package com.github.yjgbg.adserving

object biz:
  case class MaterialTemplate(val id:Long,val adxId:Long) // 素材模板
  case class Material(val id:Long,val templateId:Long)// 素材
  case class Creative(val id:Long,orderItemId:Long,materialId:Long) // 创意
  case class OrderItem(val id:Long,orderId:Long) // 订单项
  case class Order(val id:Long,advertiserId:Long) // 订单
  case class AdUnit(val id:Long,adxId:Long) // 广告位
  case class AdUnitMaterialTemplateCompatible(val id:Long,adUnitId:Long,materialTemplateId:Long)
  case class Advertiser(val id:Long,name:String) // 广告主
  case class Adx(val id:Long,name:String,code:String,eval:Targeting => Unit)
  sealed trait Targeting
  object Targeting:
    case class AdxCode(code:String) extends Targeting
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
  import core.*
  import Targeting.*
  given Ordering[Targeting] = Ordering.by(_.toString())
  val searchine = core.Searchine[Creative,Targeting]
    .load(AdxCode("IQIYI") && Network._5G,Creative(10L,10L,20L))
    .load(Gender.Male && AgeBetween(18,23) || Gender.Female && AgeBetween(23,30),Creative(20L,20L,20L))
    .load(Gender.Male && AgeBetween(18,23) ,Creative(20L,20L,30L))
    .ready
