package com.github.yjgbg.compose

case object OpenGLRuntime extends Document.Runtime:
  override def apply(application: Rx[Document.Application]): Unit = 
    val (init,render) = initAndRender
    val (state,setState) = Rx.useState(init(application.value))
    state.addListener({(current,next) => println(s"state changed from :${current} to ${next}")},state)
    setState(render(state.value,null.asInstanceOf,application.value))
    application.addListener({(current,next) => setState(render(state.value,current,next))},state)
    loop(state)
    println("finished")
  private def loop(state:Rx[Map[String,Long]]) = 
    import org.lwjgl
    import org.lwjgl.glfw
    val winSeq = state.value
    while (!winSeq.isEmpty) for ((_, winId) <- winSeq)
      if glfw.GLFW.glfwWindowShouldClose(winId) then 
        glfw.GLFW.glfwDestroyWindow(winId)
      else 
        // 在当前线程创建啥来着
        lwjgl.opengl.GL.createCapabilities()
        // RGBA 设置空的颜色
        lwjgl.opengl.GL11.glClearColor(1,0,0,0)
        lwjgl.opengl.GL11.glClear(lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT | lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT) // clear framebuffer
        glfw.GLFW.glfwSwapBuffers(winId)
        glfw.GLFW.glfwPollEvents()
  enum Modify[A]:
    case UPDATE[A](current:A,next:A) extends Modify[A]
    case CREATE[A](value:A) extends Modify[A]
    case DELETE[A](value:A) extends Modify[A]
  import Document.*
  // Map[String,Long] 是Window的业务id与opengl中的id的映射
  private def initAndRender:(Application => Map[String,Long],(Map[String,Long],Application,Application) => Map[String,Long]) = 
    import org.lwjgl.glfw
    import org.lwjgl
    (
      {app => 
        glfw.GLFWErrorCallback.createPrint(System.err).set()
        if(!glfw.GLFW.glfwInit()) throw new IllegalStateException("无法初始化GLFW")
        Map()
      },
      {(state,current,next)=> 
        val nextWinSeq = if (next.asInstanceOf[Any]!= null && next(Window)!= null) then next(Window) else Seq()
        val currentWinSeq = if (current.asInstanceOf[Any]!= null && current(Window)!= null) then current(Window) else Seq()
        // id重合的减去对象本身重合的
        val needUpdate = nextWinSeq.map(_(Id)).intersect(currentWinSeq.map(_(Id))).diff(nextWinSeq.intersect(currentWinSeq).map(_(Id)))
        val needCreate = nextWinSeq.map(_(Id)).diff(currentWinSeq.map(_(Id)))
        val needDelete = currentWinSeq.map(_(Id)).diff(nextWinSeq.map(_(Id)))
        val modify = needUpdate.map{bizId => Modify.UPDATE(currentWinSeq.find(_(Id)==bizId).get,nextWinSeq.find(_(Id)==bizId).get) -> Some(state(bizId))}
        ++ needCreate.map{(bizId) => Modify.CREATE(nextWinSeq.find(_(Id)==bizId).get) -> None}
        ++ needDelete.map{(bizId) => Modify.DELETE(currentWinSeq.find(_(Id)==bizId).get) -> Some(state(bizId))}
        modify.map{(action,option) => action match
          case Modify.DELETE(value) =>
            glfw.GLFW.glfwSetWindowShouldClose(option.get,true)
            value(Id) -> option
          case Modify.UPDATE(current, next) => 
            if (current(Title) != next(Title)) then glfw.GLFW.glfwSetWindowTitle(option.get,next(Title))
            next(Id) -> option
          case Modify.CREATE(value) => 
            import Document.Layout.Style.*
            import Document.Layout.Verb.*
            // 创建窗口
            val winId = glfw.GLFW.glfwCreateWindow(
              value(DefaultWidth),
              value(DefaultHeight),
              value(Title),
              lwjgl.system.MemoryUtil.NULL,
              lwjgl.system.MemoryUtil.NULL
            )
            value(OnInit)()
            // 配置回调函数
            glfw.GLFW.glfwSetWindowCloseCallback(winId,{(_) =>
              if (value(OnExit)!= null) value(OnExit)()
              // glfw.GLFW.glfwDestroyWindow(winId)
            })
            //long window, int key, int scancode, int action, int mods
            // 松开esc键的时候关闭窗口
            val map = if value(OnKeyRelease)!=null then value(OnKeyRelease).map{it => it(KeyCode) -> it(Callback)}.toMap else Map()
            glfw.GLFW.glfwSetKeyCallback(winId,{(_,keyId,scancode,action,mods) => 
              println(s"key clicked:winId=${winId},keyId=${keyId},action=${action},mods=${mods}")
              if action == glfw.GLFW.GLFW_RELEASE then map.get(keyId).foreach{_()}
            })
            // 设置窗口位置为根据宽高算出的屏幕中央(假定宽高不大于屏幕尺寸)
            val primaryMonitor = glfw.GLFW.glfwGetPrimaryMonitor()
            val videoMode = glfw.GLFW.glfwGetVideoMode(primaryMonitor)
            glfw.GLFW.glfwSetWindowPos(winId,(videoMode.width() - value(DefaultWidth)) / 2,(videoMode.height() - value(DefaultHeight)) / 2)
            // 将openGL的上下文设置到这个窗口开始绘制
            glfw.GLFW.glfwMakeContextCurrent(winId)
            // 开启 v-sync
            glfw.GLFW.glfwSwapInterval(0)
            glfw.GLFW.glfwShowWindow(winId)
            value(Id) -> Some(winId)
         }.filter{(k,v) => v.isDefined}.map{(k,v) => k -> v.get}.toMap
      })