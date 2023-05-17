package com.github.yjgbg.compose

trait Document extends Dsl:
  // 定义节点
  opaque type Application = collection.immutable.HashMap[Any,Any]
  given Node[Application] = Node[Application]
  opaque type Window = collection.immutable.HashMap[Any,Any]
  given Node[Window] = Node[Window]
  opaque type Dialog = collection.immutable.HashMap[Any,Any]
  given Node[Dialog] = Node[Dialog]
  opaque type Menu = collection.immutable.HashMap[Any,Any]
  given Node[Menu] = Node[Menu]
  opaque type Div = collection.immutable.HashMap[Any,Any]
  given Node[Div] = Node[Div]
  // 定义属性
  val Id = Key.Single[Window,String]("Id")
  val AutoActive = Key.Single[Window,Boolean]("AutoActive")
  val Title = Key.Single[Application|Window|Dialog|Menu,String]("Name")
  val Window = Key.Multi[Application,Window]("Window")
  val Dialog = Key.Multi[Application,Dialog]("Dialog")
  val Menu = Key.Multi[Window,Menu]("Menu")
  val Path = Key.Multi[Menu,String]("Path")
  val ShortKey = Key.Multi[Menu,Int]("ShortKey")
  val Action = Key.Single[Menu,() => Unit]("Action")
  val Scheduled = Key.Multi[Application,() => Unit]("Scheduled")
  object Layout:
    val Div = Key.Multi[Div|Window|Dialog,Div]("Div")
    val Oriential = Key.Single[Window|Div,"V"|"H"]("Oriential")
    val Theme = Key.Single[Application,String]("Theme")
    object Style:
      val DefaultWidth = Key.Single[Window|Dialog,Int]("DefaultWidth")
      val DefaultHeight = Key.Single[Window|Dialog,Int]("DefaultHeight")
      val DefaultPosition = Key.Single[Window|Dialog,(Int,Int)]("DefaultPosition")
      val Width = Key.Single[Div,Int]("Width")
      val Height = Key.Single[Div,Int]("Height")
    object Verb:
      // 最大化，最小化,以及size变化时的监听
      val OnSizeChange = Key.Single[Window,(Int,Int) => Unit]("OnSizeChange")
      val OnInit = Key.Single[Application|Window|Dialog,() => Unit]("OnInit")
      val OnExit = Key.Single[Application|Window|Dialog,() => Unit]("OnExit")
      // 单击，点击
      val OnPress = Key.Single[Div,() => Unit]("OnPress")
      // 单击，松开
      val OnRelease = Key.Single[Div,() => Unit]("OnRelease")
      // 双击
      val OnDoubleClick = Key.Single[Div,() => Unit]("OnDoubleClick")
      // 长按或鼠标右键
      val OnOptionClick = Key.Single[Div,() => Unit]("OnOptionClick")
      // 当鼠标放在该元素上时
      val OnHover = Key.Single[Div,() => Unit]("OnHover")

object Document extends Document:
  trait Runtime extends (Rx[Document.Application] => Unit):
    def Application(closure0:Scope[Application] ?=> Unit)(closure1:Scope[Application] ?=> Unit) =
      apply(RxObj[Application](closure0,closure1)(using given_Node_Application))
  val OpenGL = OpenGLRuntime