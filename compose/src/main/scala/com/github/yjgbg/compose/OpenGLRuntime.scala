package com.github.yjgbg.compose
import org.lwjgl
import org.lwjgl.glfw
import com.github.yjgbg.compose.Rx.useState
import com.github.yjgbg.compose.Document.Application
import com.github.yjgbg.compose.Document.Window
import com.github.yjgbg.compose.Document.Id
import com.github.yjgbg.compose.Document.Layout.Verb.OnInit
import com.github.yjgbg.compose.Document.Title
import com.github.yjgbg.compose.Document.Layout.Verb.OnExit

case object OpenGLRuntime extends Document.Runtime:
  val (window,setWindow) = Rx.useState(Map[String,Long]())
  override def apply(application: Rx[Document.Application]): Unit = 

    glfw.GLFWErrorCallback.createPrint(System.err).set()
    if(!glfw.GLFW.glfwInit()) throw new IllegalStateException("无法初始化GLFW")
    watchers.foreach(watcher => watcher.action(null.asInstanceOf,watcher.f(application.value)))
    watchers.foreach(watcher => application.map(watcher.f).addListener({(a0,a1) => watcher.action(a0,a1)},application))
    // var loop = true
    while(true) for ((id,winId) <- window.value) if glfw.GLFW.glfwWindowShouldClose(winId) 
    then
      glfw.GLFW.glfwDestroyWindow(winId)
      setWindow(window.value - id)
    else
      glfw.GLFW.glfwMakeContextCurrent(winId)
      lwjgl.opengl.GL.createCapabilities()
      // RGBA 设置空的颜色
      lwjgl.opengl.GL11.glClearColor(1,0,0,0)
      lwjgl.opengl.GL11.glClear(lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT | lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT) // clear framebuffer
      glfw.GLFW.glfwSwapBuffers(winId)
      glfw.GLFW.glfwPollEvents()
  trait Watcher:
    type F
    def f(application:Application):F
    def action(current:F,next:F):Unit
  object Watcher:
    def apply[F0](f0:Application => F0)(action0:(F0,F0) => Unit) = new Watcher:
      override type F = F0
      override def f(application: Document.Application): F = f0(application)
      override def action(current: F0, next: F0): Unit = action0(current,next)
  val watchers = Watcher(_(Window)){(current,next) => 
      if current!=null then println(s"current:${current.mapValues(_.json).toMap}")
      if next!=null then println(s"current:${next.mapValues(_.json).toMap}")
      // 负责创建和销毁窗口的watcher
      val current0 = if current != null then current else Map()
      val next0 = if next != null then next else Map()
      // 需要被销毁的窗口
      current0.keySet.diff(next0.keySet).foreach{(id) => 
        glfw.GLFW.glfwSetWindowShouldClose(window.value(id),true)
      }
      // 需要创建的窗口
      next0.keySet.diff(current0.keySet).foreach{(id) => 
        println(s"create window:${id}")
        val value = next0(id)
        import Document.*
        import Document.Layout.Style.*
        import Document.Layout.Verb.*
        val winId = glfw.GLFW.glfwCreateWindow(
          value(DefaultWidth),
          value(DefaultHeight),
          value(Title),
          lwjgl.system.MemoryUtil.NULL,
          lwjgl.system.MemoryUtil.NULL
        )
        if (value(OnInit)!=null) value(OnInit)
        // 配置回调函数
        glfw.GLFW.glfwSetWindowCloseCallback(winId,{(_) =>
          println(s"WindowCloseCallback:${id}")
          if (value(OnExit)!= null) value(OnExit)()
        })
        setWindow(window.value + (id -> winId))
        // 关闭垂直同步
        // 将openGL的上下文设置到这个窗口开始绘制
        glfw.GLFW.glfwMakeContextCurrent(winId)
        glfw.GLFW.glfwSwapInterval(0)
        glfw.GLFW.glfwShowWindow(winId)
      }
      current0.keySet.intersect(next0.keySet)
        .map{k => (k,current0(k),next0(k))}
        .filter{(k,current,next) => current!=next}
        .foreach{(k,current,next) => 
          println(s"update window:${k}")
          if (next(Title)!=current(Title)) glfw.GLFW.glfwSetWindowTitle(window.value(k),next(Title))  
        }
    } :: Nil