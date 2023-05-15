package com.github.yjgbg.compose

import scala.collection.mutable.WeakHashMap
import java.util.concurrent.atomic.AtomicReference
class Rx[A] private[compose](
  private[compose] var value0:A,
  private[compose] var listener: WeakHashMap[Any,(A,A) => Unit]
  )
object Rx:
  val Rx = this
  class Cancelable(val cancel : () => Unit)
  extension [A](self:A) def rx:Rx[A] = Rx(self,WeakHashMap())
  def useState[A](default:A):(Rx[A],A => Unit) = {
    val x = default.rx
    (x,{a =>
      val old = x.value0
      x.value0 = a
      if old != a then x.listener.foreach{f => f._2(old,a)}
    })
  }
  def usePeriod[A](time:Long,initial:A,closure:A => A):Rx[A] = ???
  extension [A](rx:Rx[A]) 
    def value = rx.value0
    def addListener(closure:(A,A) => Unit,key:Any = Object()):Cancelable = {
      rx.listener.put(key,closure)
      Cancelable{() => rx.listener.remove(key) }
    }
    def map[B](closure:A => B):Rx[B] = {
      val (read,write) = useState(closure(rx.value))
      addListener({(_,newValue) => write(closure(newValue))},read)
      read
    }
    def zip[B](rxb:Rx[B]):Rx[(A,B)] = {
      val (read,write) = useState((rx.value0,rxb.value0))
      addListener({(_,newValue) => write(newValue,rxb.value)},read)
      rxb.addListener({(_,newValue) => write(rx.value0,newValue)},read)
      read
    }
  extension [A](self:Rx[Rx[A]]) def flatten:Rx[A] = {
    val (read,write) = useState(self.value.value)
    val current = AtomicReference[Cancelable]
    current.set(if self.value==null then null else self.value.addListener{(oldA,newA) => write(newA)})
    self.addListener{(old,n) => 
      write(n.value)
      if(current.get()!=null) current.get().cancel()
      current.set(n.addListener{(oldA,newA) => write(newA)})
    }
    read
  }