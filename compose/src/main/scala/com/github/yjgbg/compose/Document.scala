package com.github.yjgbg.compose

import java.nio.ByteBuffer

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
  def Application(closure0:Scope[Application] ?=> Unit)(closure1:Scope[Application] ?=> Unit) =
    val rxObj = RxObj[Application](closure0,closure1)(using given_Node_Application)
    val (init,render) = initAndRender
    val (state,setState) = Rx.useState(init(rxObj.value))
    rxObj.addListener({(current,next) => setState(render(state.value,current,next))},state)
    loop(state)
  private def loop(state:Rx[Map[String,Long]]) = 
    import org.lwjgl
    import org.lwjgl.glfw
    val winSeq = state.value
    while (!winSeq.isEmpty) for ((_, winId) <- winSeq if !glfw.GLFW.glfwWindowShouldClose(winId) )
      // 在当前线程创建啥来着
      lwjgl.opengl.GL.createCapabilities()
      // RGBA 设置空的颜色
      lwjgl.opengl.GL11.glClearColor(1,0,0,0)
      lwjgl.opengl.GL11.glClear(lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT | lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT) // clear framebuffer
      glfw.GLFW.glfwSwapBuffers(winId)
      println("loop")
      glfw.GLFW.glfwPollEvents()
  enum Modify[A]:
    case UPDATE[A](current:A,next:A) extends Modify[A]
    case CREATE[A](value:A) extends Modify[A]
    case DELETE[A](value:A) extends Modify[A]
  // Map[String,Long] 是Window的业务id与opengl中的id的映射
  private def initAndRender:(Application => Map[String,Long],(Map[String,Long],Application,Application) => Map[String,Long]) = 
    import org.lwjgl.glfw
    import org.lwjgl
    (
      {app => 
        glfw.GLFWErrorCallback.createPrint(System.err).set()
        if(!glfw.GLFW.glfwInit()) throw new IllegalStateException("无法初始化GLFW")
        if(app(Window)!= null) app(Window).map{(win) => win(Id) -> processWindow(Modify.CREATE(win),None).get}.toMap else Map()
      },
      {(state,current,next)=> 
        val nextWinSeq = if (next(Window)!= null) then next(Window) else Seq()
        val currentWinSeq = if (current(Window)!= null) then current(Window) else Seq()
        // id重合的减去对象本身重合的
        val needUpdate = nextWinSeq.map(_(Id)).intersect(currentWinSeq.map(_(Id))).diff(nextWinSeq.intersect(currentWinSeq).map(_(Id)))
        val needCreate = nextWinSeq.map(_(Id)).diff(currentWinSeq.map(_(Id)))
        val needDelete = currentWinSeq.map(_(Id)).diff(nextWinSeq.map(_(Id)))
        val modify = needUpdate.map{bizId => Modify.UPDATE(currentWinSeq.find(_(Id)==bizId).get,nextWinSeq.find(_(Id)==bizId).get) -> Some(state(bizId))}
        ++ needCreate.map{(bizId) => Modify.CREATE(nextWinSeq.find(_(Id)==bizId).get) -> None}
        ++ needDelete.map{(bizId) => Modify.DELETE(currentWinSeq.find(_(Id)==bizId).get) -> Some(state(bizId))}
        modify.map{(action,option) => action match
          case Modify.UPDATE(current, next) => next(Id) -> processWindow(action,option)
          case Modify.CREATE(value) => value(Id) -> processWindow(action,option)
          case Modify.DELETE(value) => value(Id) -> processWindow(action,option)
         }.filter{(k,v) => v.isDefined}.map{(k,v) => k -> v.get}.toMap
      })

  private def processWindow(action:Modify[Window],openglId:Option[Long]):Option[Long] = 
    import org.lwjgl.glfw
    import org.lwjgl
    import Document.Layout.Style.*
    import Document.Layout.Verb.*
    action match
      case Modify.DELETE(value) => 
        glfw.GLFW.glfwSetWindowShouldClose(openglId.get,true)
        None
      case Modify.UPDATE(current, next) => 
        if (current(Title) != next(Title)) then glfw.GLFW.glfwSetWindowTitle(openglId.get,next(Title))
        openglId
      case Modify.CREATE(value) =>
        //int width, int height, @NativeType("char const *") ByteBuffer title, 
        // @NativeType("GLFWmonitor *") long monitor, @NativeType("GLFWwindow *") long share
        // 创建窗口
        val winId = glfw.GLFW.glfwCreateWindow(
          value(DefaultWidth),
          value(DefaultHeight),
          value(Title),
          lwjgl.system.MemoryUtil.NULL,
          lwjgl.system.MemoryUtil.NULL
        )
        // 配置回调函数
        glfw.GLFW.glfwSetWindowCloseCallback(winId,{(_) =>
          if (value(OnExit)!= null)value(OnExit)()
          glfw.GLFW.glfwDestroyWindow(winId)
        })
        //long window, int key, int scancode, int action, int mods
        // 松开esc键的时候关闭窗口
        glfw.GLFW.glfwSetKeyCallback(winId,{(_,keyId,scancode,action,mods) => 
          if keyId == glfw.GLFW.GLFW_KEY_ESCAPE && action == glfw.GLFW.GLFW_RELEASE then {
            glfw.GLFW.glfwSetWindowShouldClose(winId,true)
            if (value(OnExit)!= null)value(OnExit)()
            glfw.GLFW.glfwDestroyWindow(openglId.get)
          }
        })
        // 设置窗口位置为根据宽高算出的屏幕中央(假定宽高不大于屏幕尺寸)
        val primaryMonitor = glfw.GLFW.glfwGetPrimaryMonitor()
        val videoMode = glfw.GLFW.glfwGetVideoMode(primaryMonitor)
        glfw.GLFW.glfwSetWindowPos(winId,(videoMode.width() - value(DefaultWidth)) / 2,(videoMode.height() - value(DefaultHeight)) / 2)
        // 将openGL的上下文设置到这个窗口开始绘制
        glfw.GLFW.glfwMakeContextCurrent(winId)
        // 开启 v-sync
        glfw.GLFW.glfwSwapInterval(1)
        glfw.GLFW.glfwShowWindow(winId)
        Some(winId)