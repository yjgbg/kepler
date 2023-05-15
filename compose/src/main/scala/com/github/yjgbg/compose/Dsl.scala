package com.github.yjgbg.compose

import scala.annotation.unchecked.uncheckedVariance
import com.github.yjgbg.compose.Document
import java.util.concurrent.atomic.AtomicReference
import com.github.yjgbg.compose.Rx
trait Dsl:
  type Scope[+A] = AtomicReference[Rx[A @uncheckedVariance] => Rx[A @uncheckedVariance]]
  given [A] (using Scope[A]):Document.Scope[A] = ().asInstanceOf
  object Scope:
    def apply[A]:Scope[A] = AtomicReference{x => x}
  def If[A](using scope: Scope[A])
  (cond:Rx[Boolean])(closure: Scope[A] ?=> Unit):Rx[Boolean] = {
    scope.updateAndGet{f0 => rx =>
      cond.map(bool => if bool then {
        val a = f0(rx)
        val s0 = Scope[A]
        closure(using s0)
        s0.get.apply(a)
      } else {
        f0.apply(rx)
      })
      .flatten
    }
    cond
  }
  extension (rx:Rx[Boolean]) 
    infix def Else[A](using scope:Scope[A])(closure:Scope[A] ?=> Unit):Unit = 
      If(rx.map(!_))(closure)
    def ElseIf[A](using scope:Scope[A])(cond:Rx[Boolean])(closure:Scope[A] ?=> Unit):Rx[Boolean] = {
      rx.Else {If(cond)(closure)}
      rx.zip(cond).map{(b0,b1) => b0 || b1}
    }
  def For[A,B](using scope: Scope[A])(traversable:Rx[Seq[B]])(closure:Scope[A] ?=> B => Unit):Unit = {
    scope.updateAndGet{f0 => x =>
      traversable.map{seq => 
        seq.map { b =>
          val scope = Scope[A]
          val x = closure(using scope)(b)
          scope.get()
        }
        .fold(a => a)(_.andThen(_))
      }.map(f => f(f0(x))).flatten
    }
  }
  def Application(using Document.Constructor[Document.Application])(closure0:Scope[Document.Application] ?=> Unit)(closure1:Scope[Document.Application] ?=> Unit):Rx[Document.Application] = {
    val x = Scope[Document.Application]
    closure0(using x)
    closure1(using x)
    val con = summon[Document.Constructor[Document.Application]].getInstance
    import Rx.rx
    x.get().apply(con.rx)
  }
  
  extension [A,B](key:Document.Key[A,B]) def :=[C<:String&Singleton]
    (using Scope[Document.Node[C]],Document.Node[C] <:< A)
    (value:Rx[B]|B):Unit = value match
      // 此处的警告是因为不能用case检查出B的具体类型，但是没关系
      case rxb:Rx[B] => summon[Scope[Document.Node[C]]].updateAndGet{fun => rxa =>
        fun(rxa).zip(rxb).map{(a,b) => a.addProp[B](key.asInstanceOf[Document.Key[Document.Node[C],B]],b)}
      }
      case b:B => summon[Scope[Document.Node[C]]].updateAndGet{fun => rxa =>
        fun(rxa).map{_.addProp[B](key.asInstanceOf[Document.Key[Document.Node[C],B]],b)}
      }
  extension [A,B<:Document.Node[_]](key:Document.Key[A,Seq[B]]|Document.Key[A,B]) 
    def apply[C<:String&Singleton,D <:A & Document.Node[C]]
    (using Scope[D],Document.Node[C] =:= D,Document.Constructor[B])
    (closure0:Scope[B] ?=> Unit = {(ignored:Scope[B]) ?=> })
    (closure1:Scope[B] ?=> Unit):Unit = 
      summon[Scope[D]].updateAndGet{fun =>
        val scopeB = Scope[B]
        closure0(using scopeB)
        closure1(using scopeB)
        val x = summon[Document.Constructor[B]].getInstance
        import Rx.rx
        val rxb = scopeB.get().apply(x.rx)
        key match 
          case seq:Document.KeySeq[Document.Node[C],B] => 
            rxd => fun(rxd).zip(rxb).map{(d,b) => d.addProp(seq,d.get[Seq[B]](seq) :+ b)}
          case single:Document.Key[Document.Node[C],B] => 
            rxd => fun(rxd).zip(rxb).map{(d,b) => d.addProp(single,b)}
            
      }
object Dsl extends Dsl