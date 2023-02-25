package com.github.yjgbg.adserving

object core:
  case class Assignment[A](flag:Boolean,it:A)
  opaque type Conjunction[A] = Seq[Assignment[A]]
  opaque type DNF[A] = Seq[Conjunction[A]]
  object DNF:
    def apply[A](it:Seq[Seq[Assignment[A]]]):DNF[A] = it
  // 命题的真值，真，假，无法判定
  // 其对应的否命题真值分别为： 假，真，无法判定
  type ER = Boolean | Null
  opaque type Evaluator[A,F[_]] = A => F[ER]
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
    def apply[A,F[_]](func:A => F[ER]):Evaluator[A,F] = func
  import collection.mutable
  import scala.language.experimental.erasedDefinitions
  enum Ready:
    case Yes extends Ready
    case No extends Ready
  case class Item[A](data:A,priority:Long = 0)
  case class Searchine[E,T:Ordering,A <: Ready](val store:mutable.HashMap[Seq[Assignment[T]],mutable.Buffer[Item[E]]]):
    import scala.language.experimental.erasedDefinitions
    private lazy val conjVector:Vector[Conjunction[T]] = store.keySet.to(Vector)
    private lazy val tVector:Vector[T] = conjVector.flatMap(_.map(_.it))
    private given Ordering[Item[_]] = Ordering.by(_.priority)
    private given Ordering[Assignment[T]] = Ordering.by[Assignment[T],T](_.it).orElseBy(_.flag)
    def load(using erased A =:= Ready.No.type)(cond: DNF[T]|T, e: E, priority: Long = 0): Searchine[E,T,Ready.No.type] = 
      cond match
        case dnf:Seq[Seq[Assignment[T]]] => dnf.foreach{conj => 
            store.getOrElseUpdate(conj.sorted,mutable.Buffer[Item[E]]()) += Item(e,priority)
          }
          this.asInstanceOf[Searchine[E,T,Ready.No.type]]
        case t:T => load(Seq(Seq(Assignment(true,t))),e,priority)
    def ready(using erased A =:= Ready.No.type):Searchine[E,T,Ready.Yes.type] = {
      // 排序
      this.store.mapValuesInPlace{(k,v) => v.sorted}
      // 触发lazy的计算
      this.tVector
      // 返回这个对象
      this.asInstanceOf[Searchine[E,T,Ready.Yes.type]]
    }
    import cats.syntax.all.{*,given}
    def search[F[_]:cats.Applicative:cats.Functor](using erased A =:= Ready.Yes.type)(limit: Int, evaluator: Evaluator[T,F]): F[Seq[Item[E]]] =
      this.tVector
        .traverse{it => evaluator(it).map{it -> _}}
        .map{_.toMap}
        .map{ res => this.conjVector
          .filter{conj => conj.forall{ass => res(ass.it) match
            case null => false
            case b:Boolean => !ass.flag ^ b
          }}
          .flatMap{this.store.getOrElse(_,Seq[Item[E]]()).take(limit)}
          .sorted
          .take(limit)
        }
  object Searchine:
    def apply[E,T:Ordering] = new Searchine[E,T,Ready.No.type](mutable.HashMap())
    