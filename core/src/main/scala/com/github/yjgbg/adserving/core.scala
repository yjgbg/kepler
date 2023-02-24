package com.github.yjgbg.adserving

object core:
  case class Assignment[A](flag:Boolean,it:A)
  opaque type Conjunction[A] = Seq[Assignment[A]]
  opaque type DNF[A] = Seq[Conjunction[A]]
  object DNF:
    def apply[A](it:Seq[Seq[Assignment[A]]]):DNF[A] = it
  opaque type Evaluator[A,F[_]] = PartialFunction[A,F[Boolean]]
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
    def apply[A,F[_]](func:PartialFunction[A,F[Boolean]]):Evaluator[A,F] = func
  import collection.mutable
  enum State:
    case Loading extends State
    case Biding extends State
  case class Item[A](data:A,priority:Long = 0)
  given Ordering[Item[_]] = Ordering.by(- _.priority)
  case class Searchine[E,T:Ordering,A <: State](val store:mutable.HashMap[Seq[Assignment[T]],mutable.Buffer[Item[E]]]):
    private lazy val conjVector = store.keySet.to(Vector)
    private lazy val tVector = conjVector.flatMap(_.map(_.it))
  object Searchine:
    def apply[E,T:Ordering]:Searchine[E,T,State.Loading.type] = new Searchine(mutable.HashMap())
    private given [T:Ordering]:Ordering[Assignment[T]] = 
      Ordering.by[Assignment[T],T](_.it).orElseBy(_.flag)
    extension [E,T:Ordering](that:Searchine[E,T,State.Loading.type]) 
      @scala.annotation.nowarn def load(cond: DNF[T]|T, e: E, priority: Long = 0): Searchine[E,T,State.Loading.type] = 
        cond match
          case dnf:Seq[Seq[Assignment[T]]] => dnf.foreach{conj => 
              that.store.getOrElseUpdate(conj.sorted,mutable.Buffer[Item[E]]()) += Item(e,priority)
            }
            that
          case t:T => load(Seq(Seq(Assignment(true,t))),e,priority)
      def ready:Searchine[E,T,State.Biding.type] = {
        // 排序
        that.store.mapValuesInPlace{(k,v) => v.sorted}
        // 触发lazy的计算
        that.tVector
        // 返回这个对象
        that.asInstanceOf[Searchine[E,T,State.Biding.type]]
      }
    import cats.syntax.all.{*,given}
    extension [E,T:Ordering](that:Searchine[E,T,State.Biding.type])
      def search[F[_]:cats.Applicative:cats.Functor](limit: Int, evaluator: Evaluator[T,F]): F[Seq[Item[E]]] =
        that.tVector
          .traverse{it => evaluator(it).map{it -> _}}
          .map{_.toMap}
          .map{ res => that.conjVector
            .filter{conj => conj.forall{ass => !ass.flag ^ res(ass.it)}}
            .flatMap{that.store.getOrElse(_,Seq[Item[E]]()).take(limit)}
            .sorted
            .take(limit)
          }
