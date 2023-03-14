package com.github.yjgbg.adserving

import scala.collection.mutable.SortedSet
import scala.collection.mutable.HashSet
import com.github.yjgbg.adserving.utils.objectMapper
import com.github.yjgbg.adserving.web.AppConfig

object engine:
  case class Assignment[+A](flag:Boolean,it:A)
  opaque type Conjunction[+A] = Seq[Assignment[A]]
  opaque type DNF[+A] = Seq[Conjunction[A]]
  object DNF:
    @annotation.nowarn def apply[A](dnfOrA:DNF[A]|Seq[Seq[Assignment[A]]]|A):DNF[A] = 
      dnfOrA match
        case dnf:Seq[Seq[Assignment[A]]] => dnf
        case a:A => Seq(Seq(Assignment(true,a)))
    def any[A:Ordering](a:IterableOnce[DNF[A]|A]):DNF[A]|Null = 
      a.iterator.map(DNF.apply).reduceOption{(a,b) => a || b}.orNull
    def all[A:Ordering](a:IterableOnce[DNF[A]|A]):DNF[A]|Null = 
      a.iterator.map(DNF.apply).reduceOption{(a,b) => a && b}.orNull
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
  // 数据本身，优先级，以及剩余可被search到的次数
  case class Item[A](data:A,priority:Long = 0) extends Ordered[Item[A]]:
    // times为0者小于times不为0者，times都为0或都不为0，则按照优先级比较
    override def compare(that: Item[A]): Int =  
      this.priority.compare(that.priority)
  case class Statistic[E](data:E,times:Long)
  case class Record[T,E](id:String,evaluateResult:Map[T,Boolean|Null],result:Seq[Item[E]])
  case class SearchingZone[E,T](val key:String):
    private[engine] lazy val store:mutable.HashMap[Conjunction[T],mutable.Buffer[Item[E]]] = mutable.HashMap()
    private[engine] lazy val conjVector:Vector[Conjunction[T]] = store.keySet.to(Vector)
    private[engine] lazy val tVector:Vector[T] = conjVector.flatMap(_.map(_.it))
  object Searchine:
    def apply[E,T<: Matchable : Ordering] = new Searchine[E,T,Ready.No.type]
  sealed class Searchine[E,T<: Matchable :Ordering,A<:Ready] private:
    private val hash:mutable.HashMap[String,SearchingZone[E,T]] = mutable.HashMap()
    private var _limit:mutable.Map[E=>Boolean,Long] = mutable.HashMap()
    private lazy val limit:mutable.Map[HashSet[E],Long] = {
      val allE = hash.values.flatMap(se => se.store.values)
        .flatMap(_.iterator).map(_.data).to(HashSet)
      _limit
        .map{(selector,times) => allE.filter(selector) -> times}
        .to(mutable.HashMap)
    }
    val _statistic = mutable.HashMap[E,Long]()
    val statistic:Map[E,Long] = _statistic.to(Map)
    def ready(using /*erased*/ A =:= Ready.No.type):Searchine[E,T,Ready.Yes.type] = 
      hash.values.foreach{sz => 
        // 排序
        sz.store.mapValuesInPlace{(k,v) => v.sorted}
        // 触发lazy
        sz.tVector
      }
      // 触发lazy
      limit
      this.asInstanceOf[Searchine[E,T,Ready.Yes.type]]
    private given Ordering[Assignment[T]] = 
      Ordering.by[Assignment[T],T](_.it).orElseBy(_.flag)
    @annotation.nowarn def load(using /*erased*/ A =:= Ready.No.type)
    (zoneKey:String,cond: DNF[T]|T, e: E,priority: Long = 0):Searchine[E,T,A] = 
      val item = Item(e,priority)
      val searchingZone = hash.getOrElseUpdate(zoneKey,SearchingZone[E,T](zoneKey))
      cond match
        case dnf:DNF[T] => dnf.foreach{conj => searchingZone.store
          .getOrElseUpdate(conj.sorted,mutable.Buffer[Item[E]]())
          += item
        }
        case t:T => searchingZone.store
          .getOrElseUpdate(Seq(Assignment(true,t)),mutable.Buffer[Item[E]]())
          += item
      this
    def limit(using /*erased*/ A =:= Ready.No.type)
    (count:Long)(selector:E=>Boolean):Searchine[E,T,A] = 
      _limit += (selector -> count)
      this
    def search(using /*erased*/ A =:= Ready.Yes.type)
    (id:String,zoneKey:String,limit: Int, evaluator: Evaluator[T]) = for {
      appConfig <- zio.ZIO.service[AppConfig]
      x = hash.get(zoneKey)
      y = for {
        sz <- x
        evaluateResult = sz.tVector.map(it => it -> evaluator(it)).toMap
        sorted = for {
          conj if conj.forall{_ => true} <- sz.conjVector

        }
      } yield {
        val res = sz.conjVector
          .filter{conj => conj.forall{ass => evaluateResult(ass.it) match
            case null => false
            case b:Boolean => !ass.flag ^ b
          }}
          .flatMap{sz.store.getOrElse(_,Seq())}
          .sorted
          .to(LazyList)
          // 筛选出尚未达到投放限制的
          .filter{item => this.limit.forall{(k,v) => 
            val res = if (!k.contains(item.data)) true 
            else if (v <= 0) false
            else {this.limit.updateWith(k)(_.map(_ - 1));true}
            if (res) _statistic.updateWith(item.data){
              case None => Some(1)
              case Some(value) => Some(value + 1)
            }
            res
          }}
          .take(limit)
        scribe.info(objectMapper.writeValueAsString(Record(id,evaluateResult,res)).nn)
        res
      }
    } yield x
      