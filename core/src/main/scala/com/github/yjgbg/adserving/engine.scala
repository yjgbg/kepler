package com.github.yjgbg.adserving

import scala.reflect.ClassTag
import scala.reflect.TypeTest

object engine:
  case class Assignment[+A](flag:Boolean,it:A)
  opaque type Conjunction[+A] = Seq[Assignment[A]]
  opaque type DNF[+A] = Seq[Conjunction[A]]
  object DNF:
    def apply[A](it:Seq[Seq[Assignment[A]]]):DNF[A] = it
  // 命题的真值，真，假，无法判定
  // 其对应的否命题真值分别为： 假，真，无法判定
  type ER = Boolean | Null
  opaque type Evaluator[A] = A => ER
  extension [A:Ordering](self:DNF[A]|A)
    @annotation.nowarn def unary_! : DNF[A] = self match
      case dnf:DNF[A] => dnf.map{conj => conj.toSeq.map{case Assignment(flag,it) => Seq(Assignment(!flag,it))}}.reduce(_ && _)
      case a:A => Seq(Seq(Assignment(false,a)))
    @annotation.nowarn def ||(it:DNF[A]|A):DNF[A] = (self -> it) match
      case (dnf0:DNF[A],dnf1:DNF[A]) => dnf0 ++ dnf1
      case (dnf0:DNF[A],a1:A) => dnf0 :+ Seq(Assignment(true,a1))
      case (a0:A,dnf1:DNF[A]) => Seq(Assignment(true,a0)) +: dnf1
      case (a0:A,a1:A) => Seq(Assignment(true,a0)) +: Seq(Assignment(true,a1)) +: Nil
    @annotation.nowarn def &&(it:DNF[A]|A):DNF[A] = (self -> it) match
      case (dnf0:DNF[A],dnf1:DNF[A]) => (for {
        conj0 <- dnf0
        conj1 <- dnf1
      } yield (conj0 ++ conj1))
      case (dnf0:DNF[A],a1:A) => dnf0.map{_  :+ (Assignment(true,a1))}
      case (a0:A,dnf1:DNF[A]) =>  dnf1.map{(Assignment(true,a0)) +: _}
      case (a0:A,a1:A) => Seq(Seq(Assignment(true,a0),Assignment(true,a1)))
  object Evaludator:
    def apply[A](func:A => ER):Evaluator[A] = func
  import collection.mutable
  enum Ready:
    case Yes extends Ready
    case No extends Ready
  case class Item[A](data:A,priority:Long = 0) extends Ordered[Item[A]]:
    override def compare(that: Item[A]): Int = this.priority.compare(that.priority)
    
  case class SearchingZone[E,T](val key:String):
    private[engine] lazy val store:mutable.HashMap[Seq[Assignment[T]],mutable.Buffer[Item[E]]] = mutable.HashMap()
    private[engine] lazy val conjVector:Vector[Conjunction[T]] = store.keySet.to(Vector)
    private[engine] lazy val tVector:Vector[T] = conjVector.flatMap(_.map(_.it))
  object Searchine:
    def apply[E,T<: Matchable : Ordering:ClassTag] = new Searchine[E,T,Ready.No.type]
  sealed class Searchine[E,T<: Matchable :Ordering:ClassTag,A<:Ready] private:
    val hash:mutable.HashMap[String,SearchingZone[E,T]] = mutable.HashMap()
    def ready(using /*erased*/ A =:= Ready.No.type):Searchine[E,T,Ready.Yes.type] = 
      hash.values.foreach{sz => 
        // 排序
        sz.store.mapValuesInPlace{(k,v) => v.sorted}
        // 触发lazy的计算
        sz.tVector
      }
      this.asInstanceOf[Searchine[E,T,Ready.Yes.type]]
    private given Ordering[Assignment[T]] = 
      Ordering.by[Assignment[T],T](_.it).orElseBy(_.flag)
    def load(using /*erased*/ A =:= Ready.No.type)
    (zoneKey:String,cond: DNF[T]|T, e: E, priority: Long = 0):Searchine[E,T,A] = 
      val searchingZone = hash.getOrElseUpdate(zoneKey,{SearchingZone[E,T](zoneKey)})
      cond match
        case dnf:DNF[T] => dnf.foreach{conj => searchingZone.store
          .getOrElseUpdate(conj.sorted,mutable.Buffer[Item[E]]()) += Item(e,priority)
        }
        case t:T => searchingZone.store
          .getOrElseUpdate(Seq(Assignment(true,t)),mutable.Buffer[Item[E]]()) += Item(e,priority)
      this
    def search(using /*erased*/ A =:= Ready.Yes.type)
    (zoneKey:String,limit: Int, evaluator: Evaluator[T]):Seq[Item[E]] =
      hash.get(zoneKey).map{ sz =>
        val res = sz.tVector.map(it => it -> evaluator(it)).toMap
        sz.conjVector.filter{conj => conj.forall{ass => res(ass.it) match
          case null => false
          case b:Boolean => !ass.flag ^ b
        }}
        .flatMap{sz.store.getOrElse(_,Seq()).take(limit)}
        .sorted
        .take(limit)
      }.getOrElse(Seq())