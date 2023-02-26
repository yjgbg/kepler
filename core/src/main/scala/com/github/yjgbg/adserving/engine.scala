package com.github.yjgbg.adserving

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
  case class Item[A](data:A,priority:Long = 0)
  class Searchine[E,T:Ordering,A <: Ready]:
    private val store:mutable.HashMap[Seq[Assignment[T]],mutable.Buffer[Item[E]]] = 
      mutable.HashMap()
    private lazy val conjVector:Vector[Conjunction[T]] = 
      store.keySet.to(Vector)
    private lazy val tVector:Vector[T] = 
      conjVector.flatMap(_.map(_.it))
    private given Ordering[Item[_]] = 
      Ordering.by(_.priority)
    private given Ordering[Assignment[T]] = 
      Ordering.by[Assignment[T],T](_.it).orElseBy(_.flag)
    @annotation.nowarn def load(using /*erased*/ A =:= Ready.No.type)
    (cond: DNF[T]|T, e: E, priority: Long = 0): Searchine[E,T,Ready.No.type] = 
      cond match
        case dnf:Seq[Seq[Assignment[T]]] => dnf.foreach{conj => 
            store.getOrElseUpdate(conj.sorted,mutable.Buffer[Item[E]]()) += Item(e,priority)
          }
          this.asInstanceOf[Searchine[E,T,Ready.No.type]]
        case t:T => load(Seq(Seq(Assignment(true,t))),e,priority)
    def ready(using /*erased*/ A =:= Ready.No.type):Searchine[E,T,Ready.Yes.type] = {
      // 排序
      this.store.mapValuesInPlace{(k,v) => v.sorted}
      // 触发lazy的计算
      this.tVector
      // 返回这个对象
      this.asInstanceOf[Searchine[E,T,Ready.Yes.type]]
    }
    def search(using /*erased*/ A =:= Ready.Yes.type)(limit: Int, evaluator: Evaluator[T]): Seq[Item[E]] =
      val res = tVector.map(it => it -> evaluator(it)).toMap
      conjVector
        .filter{conj => conj.forall{ass => res(ass.it) match
          case null => false
          case b:Boolean => !ass.flag ^ b
        }}
        .flatMap{store.getOrElse(_,Seq()).take(limit)}
        .sorted
        .take(limit)
  object Searchine:
    def apply[E,T:Ordering] = new Searchine[E,T,Ready.No.type]
    def zoned[E,T:Ordering] = new ZonedSearchine[E,T,Ready.No.type]

  class ZonedSearchine[E,T:Ordering,A<:Ready]:
    val hash:mutable.HashMap[String,Searchine[E,T,A]] = mutable.HashMap()
    def ready(using /*erased*/ A =:= Ready.No.type)
    :ZonedSearchine[E,T,Ready.Yes.type] = 
      hash.values.foreach(_.ready)
      this.asInstanceOf[ZonedSearchine[E,T,Ready.Yes.type]]
    def load(using /*erased*/ A =:= Ready.No.type)
    (zoneKey:String,cond: DNF[T]|T, e: E, priority: Long = 0):ZonedSearchine[E,T,A] = 
      hash
        .getOrElseUpdate(zoneKey,{Searchine[E,T].asInstanceOf[Searchine[E,T,A]]})
        .load(cond,e,priority)
      this
    def search(using /*erased*/ A =:= Ready.Yes.type)
    (zoneKey:String,limit: Int, evaluator: Evaluator[T]): Seq[Item[E]] =
      hash.get(zoneKey).map(_.search(limit,evaluator)).getOrElse(Seq())