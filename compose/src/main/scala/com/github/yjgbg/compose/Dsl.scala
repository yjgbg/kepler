package com.github.yjgbg.compose

import scala.annotation.unchecked.uncheckedVariance
import java.util.concurrent.atomic.AtomicReference
import com.github.yjgbg.compose.Rx
trait Dsl:
  trait Node[A]:
    def empty:A
    extension (that:A)
      def json:String
      def apply[B](key:Key[_ >: A,B]):B
      def apply[B](key:Key[_ >: A,B],value:B):A
  object Node:
    def apply[A](using A =:= collection.immutable.HashMap[Any,Any]):Node[A] = new Node[A]:
      override def empty:A = collection.immutable.HashMap[Any,Any]().asInstanceOf[A]
      extension (that: A) override def apply[B](key: Key[? >: A, B]): B = that.getOrElse(key,null).asInstanceOf
      extension (that: A) override def apply[B](key: Key[? >: A, B], value: B): A = (that + (key -> value).asInstanceOf[(Any,Any)]).asInstanceOf
      extension (that: A) override def json: String = json0(that)
      private def json0(any:Any):String = any match
        case seq:Seq[_] => seq.map{json0(_)}.mkString("[",",","]")
        case map:collection.immutable.HashMap[?,?] => map.map{(k,v) => json0(k) +":" + json0(v)}.mkString("{",",","}")
        case string:String => "\""+string+"\""
        case bool:Boolean => bool.toString()
        case num:Number => num.toString()
        case null => "null"
        case key:Key.Single[?,?] => "\""+key.name+"\""
        case key:Key.Multi[?,?] => "\""+key.name+"\""
        case _ => "\""+any.getClass()+"\""
  type Elem[A] = A match
    case Seq[a] => a
    case _ => A
  sealed trait Key[A,B]
  object Key:
    case class Single[A,B](name:String) extends Key[A,B]
    case class Multi[A,B](name:String) extends Key[A,Seq[B]]
    inline def apply[A,B](name:String) = inline compiletime.erasedValue[B] match
      case _:Seq[a] => Key.Multi[A,Elem[B]](name):Key.Multi[A,Elem[B]]
      case _ => Key.Single[A,B](name):Key.Single[A,B]
  opaque type Scope[A] = AtomicReference[Rx[A] => Rx[A]]
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
  def RxObj[A:Node](closure:Scope[A] ?=> Unit):Rx[A] = {
    val x:Scope[A] = AtomicReference(it => it)
    closure(using x)
    val unitA = summon[Node[A]].empty
    import Rx.rx
    x.get().apply(unitA.rx)
  }
  
  extension [A,B](key:Key[A,B]) def :=[C <: A : Node]
    (using Scope[C])(value:Rx[B]|B):Unit = value match
      // 此处的警告是因为不能用case检查出B的具体类型，但是没关系
      case rxb:Rx[B] @unchecked => summon[Scope[C]].updateAndGet{fun => rxa =>
        fun(rxa).zip(rxb).map{(a,b) => a(key,b)}
      }
      case b:B @unchecked => summon[Scope[C]].updateAndGet{fun => rxa =>
        fun(rxa).map{a => a(key,b)}
      }
  extension [A,B:Node](key:Key.Single[A,B]|Key.Multi[A,B]) 
    def apply[C <: A : Node](using Scope[C])
    (closure0:Scope[B] ?=> Unit = {(ignored:Scope[B]) ?=> })
    (closure1:Scope[B] ?=> Unit):Unit = 
      summon[Scope[C]].updateAndGet{fun =>
        val scopeB = Scope[B]
        closure0(using scopeB)
        closure1(using scopeB)
        import Rx.rx
        val rxb = scopeB.get().apply(summon[Node[B]].empty.rx)
        key match 
          case single:Key.Single[C,B] @unchecked => rxa => 
            fun(rxa).zip(rxb).map{(a,b) => a(single,b)}
          case multi:Key.Multi[C,B] @unchecked => rxa => 
            fun(rxa).zip(rxb).map{(a,b) =>
              val seq = a(multi)
              if seq == null then a(multi,Seq(b)) else a(multi,seq :+ b)
            }
          case _ => throw new IllegalAccessError()
      }
object Dsl extends Dsl