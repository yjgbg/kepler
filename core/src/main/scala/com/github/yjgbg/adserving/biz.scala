package com.github.yjgbg.adserving

object biz:
  case class MaterialTemplate(val id:Long,val adxId:Long) // 素材模板
  case class Material(val id:Long,val templateId:Long)// 素材
  case class Creative(val id:Long,orderItemId:Long,materialId:Long,price:Long) // 创意
  case class OrderItem(val id:Long,orderId:Long) // 订单项
  case class Order(val id:Long,advertiserId:Long) // 订单
  case class AdUnit(val id:Long,adxId:Long) // 广告位
  case class AdUnitMaterialTemplateCompatible(val id:Long,adUnitId:Long,materialTemplateId:Long)
  case class Advertiser(val id:Long,name:String) // 广告主
  case class Adx(val id:Long,name:String,code:String,eval:Targeting => Unit)
  sealed trait Targeting
  given [A <: Targeting]:Ordering[A] = Ordering.by(_.toString())
  object Targeting:
    case class AdxCode(code:String) extends Targeting
    case class Nid(nid:String) extends Targeting
    case class DealCode(code:String) extends Targeting
    enum Gender extends Targeting:
      case Male extends Gender
      case Female extends Gender
    enum Network extends Targeting:
      case Network_WIFI extends Network
      case Network3G extends Network
      case Network4G extends Network
      case Network5G extends Network
    case class Geo(province:String,city:String) extends Targeting // 地域定向
    case class AgeBetween(min:Int,max:Int) extends Targeting // 年龄范围定向
    enum IdType extends Targeting: // ID类型定向
      case IMEI extends IdType
      case OAID extends IdType
      case IDFA extends IdType
      case COOKIE extends IdType
      case UNKNOWN extends IdType
    case class MD5[A <: IdType](idType:A) extends Targeting
    case class Pkg[A<:IdType](idType:A,pkgId:Long) extends Targeting // 人群包定向
    sealed trait OS extends Targeting
    object OS:
      enum ANDROID extends OS:
        case ANDROID6 extends ANDROID
        case ANDROID7 extends ANDROID
        case ANDROID8 extends ANDROID
        case ANDROID9 extends ANDROID
        case ANDROID10 extends ANDROID
        case ANDROID11 extends ANDROID
        case ANDROID12 extends ANDROID
        case ANDROID13 extends ANDROID
      enum IOS extends OS:
        case IOS6 extends IOS
        case IOS7 extends IOS
        case IOS8 extends IOS
        case IOS9 extends IOS
        case IOS10 extends IOS
        case IOS11 extends IOS
        case IOS12 extends IOS
        case IOS13 extends IOS
        case IOS14 extends IOS
        case IOS15 extends IOS
        case IOS16 extends IOS
      case object WINDOWS extends OS
      case object WINDOWSPHONE extends OS

