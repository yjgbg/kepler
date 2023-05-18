package com.github.yjgbg.compose

import java.util.concurrent.atomic.AtomicReference
import scala.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import java.{util => ju}
import com.github.yjgbg.compose.Rx.WeakEntry
class Rx[A] private[compose](
  private[compose] var value0:A,
  private[compose] var listener: CopyOnWriteArrayList[WeakEntry[Any,(A,A) => Unit]]
  )
object Rx:
  case class WeakEntry[A,B](key:A,value:B) extends java.lang.ref.WeakReference[A](key)
  val Rx = this
  class Cancelable(val cancel : () => Unit)
  extension [A](self:A) def rx:Rx[A] = Rx(self,CopyOnWriteArrayList())
  def useState[A](default:A):(Rx[A],A => Unit) = 
    val x = default.rx
    (x,{a =>if x.value0 != a then {
      val old = x.value0
      x.value0 = a
      x.listener.forEach{entry => if  entry.get() != null then entry.value(old,a)}
    }})

  def usePeriod[A](time:Long,initial:A,closure:A => A):Rx[A] = {
    val (state,setState) = useState(initial)
    new Thread(() => {
      while (true) {
        Thread.sleep(time)
        setState(closure(state.value))
      }
    }).start()
    state
  }
  extension [A](rx:Rx[A]) 
    def value = rx.value0
    def addListener(closure:(A,A) => Unit,key:Any):Cancelable =
      val weakEntry = WeakEntry(key,closure)
      rx.listener.add(weakEntry)
      rx.listener.removeIf{_.get()==null}
      Cancelable{() => rx.listener.remove(weakEntry)}
    def map[B](closure:A => B):Rx[B] =
      val (read,write) = useState(closure(rx.value))
      addListener({(_,newValue) => write(closure(newValue))},read)
      read
    def mapFunctor[M[_]:cats.Functor,B](unit:B,closure:A => M[B]):Rx[B] =
      val (read,write) = useState(unit)
      addListener({(_,newValue) => 
        val m = closure(newValue)
        cats.Functor[M].map(m){b => write(b)}
      },read)
      read
    def zip[B](rxb:Rx[B]):Rx[(A,B)] = 
      val (read,write) = useState((rx.value0,rxb.value0))
      addListener({(_,newValue) => write(newValue,rxb.value)},read)
      rxb.addListener({(_,newValue) => write(rx.value0,newValue)},read)
      read
    def reduce[B](init:B,closure:(B,A) => B):Rx[B] = 
      val (read,write) = useState(init)
      addListener({(current,next) => 
        write(closure(read.value,next))  
      },read)
      read
  extension [A](self:Rx[Rx[A]]) def flatten:Rx[A] =
    val (read,write) = useState(self.value.value)
    val current = AtomicReference[Cancelable]
    current.set(if self.value==null then null else self.value.addListener({(oldA,newA) => write(newA)},read))
    self.addListener({(old,n) => 
      write(n.value)
      if(current.get()!=null) current.get().cancel()
      current.set(n.addListener({(oldA,newA) => write(newA)},read))
    },read)
    read