package com.github.yjgbg.compose

object Document:
  opaque type Scope[A] = Unit
  class Key[-A,B](val name:String) // 应该加个name参数，把下面的方法名传进来，用lihaoyi.sourcecode库
  class KeySeq[A,B](val name0:String) extends Key[A,Seq[B]](name0)
  object Key:
    transparent inline def apply[A,B](name:String) = inline compiletime.erasedValue[B] match
      case _:Seq[a] => new KeySeq[A,a](name)
      case _ => new Key[A,B](name)

  type Prop[-A,B] = (Key[A,B],B)
  type Props[-A] = Seq[Prop[A,Any]]
  case class Node[A <: String & Singleton](tpe:A,prop:Props[Node[A]]):
    def show:String = s"$tpe(${prop0.map{(k,v) => s"${k.name}:${v match
      case node:Node[?] => node.show
      case seq:Seq[a] => seq.map{(it) => it match
        case node:Node[?] => node.show
        case _:Any => it.toString()
      }.mkString(",")
      case _: Any => v.toString()
    }"}.mkString(",")})"
    lazy val prop0 = prop.toMap
    def get[B](key:Key[Node[A],B]):B = key match
      case keySeq:KeySeq[Node[A], Any] => 
        prop0.getOrElse(keySeq.asInstanceOf[Key[Node[A],Any]],Seq()).asInstanceOf[B]
      case key:Key[?,?] => 
        prop0.getOrElse(key.asInstanceOf,null).asInstanceOf[B]
    def addProp[B](key:Key[Node[A],B],value:B):Node[A] = {
      val oldProp = this.prop
      val newProp = key.asInstanceOf[Key[Node[A],Any]] -> value.asInstanceOf[Any]
      new Node[A](tpe,oldProp :+ newProp)
    }
  opaque type Constructor[A<:Node[_]] = () => A
  extension [A<:Document.Node[_]](cons:Constructor[A]) def getInstance:A = cons.apply
  inline given [A<:Node[_]]:Constructor[A] = () => new Node(compiletime.constValue[A match
    case Node[a] => a
  ].asInstanceOf,Seq()).asInstanceOf
  type Application = Node["Application"]
  type Window = Node["Window"]
  type Dialog = Node["Dialog"]
  type Menu = Node["Menu"]
  type Div = Node["Div"]
  val Name = Key[Application|Window|Dialog|Menu,String]("Name")
  // def Name[A<:Application|Window|Dialog|Menu](using Scope[A]):Key[A,String] = Key()
  val Window = Key[Application,Seq[Window]]("Window")
  val Dialog = Key[Application,Seq[Dialog]]("Dialog")
  val Menu = Key[Window,Seq[Menu]]("Menu")
  val Path = Key[Menu,Seq[String]]("Path")
  val ShortKey = Key[Menu,Seq[Int]]("ShortKey")
  val Action = Key[Menu,() => Unit]("Action")
  val Scheduled = Key[Application,Seq[() => Unit]]("Scheduled")
  object Layout:
    val Div = Key[Div|Window|Dialog,Seq[Div]]("Div")
    val Oriential = Key[Window|Div,"V"|"H"]("Oriential")
    val Theme = Key[Application,String]("Theme")
    object Style:
      val DefaultWidth = Key[Window|Dialog,Int]("DefaultWidth")
      val DefaultHeight = Key[Window|Dialog,Int]("DefaultHeight")
      val Width = Key[Div,Int]("Width")
      val Height = Key[Div,Int]("Height")
    object Verb:
      // 最大化，最小化,以及size变化时的监听
      val OnSizeChange = Key[Window,(Int,Int) => Unit]("OnSizeChange")
      val OnExit = Key[Application|Window|Dialog,() => Unit]("OnExit")
      // 单击，点击
      val OnPress = Key[Div,() => Unit]("OnPress")
      // 单击，松开
      val OnRelease = Key[Div,() => Unit]("OnRelease")
      // 双击
      val OnDoubleClick = Key[Div,() => Unit]("OnDoubleClick")
      // 长按或鼠标右键
      val OnOptionClick = Key[Div,() => Unit]("OnOptionClick")
      // 当鼠标放在该元素上时
      val OnHover = Key[Div,() => Unit]("OnHover")
    // val Text = Key[Layout,String]("Text")
    // val Img = Key[Layout,String]("Img")
    // val OpenGL = Key[Layout,String]("OpenGL")
    // val Vulkan = Key[Layout,() => Unit]("Vulkan")
    // val Skija = Key[Layout,() => Unit]("Skija")