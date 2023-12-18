import java.lang.ref.ReferenceQueue
import java.lang.ref.PhantomReference
import java.util.concurrent.Executors
import java.lang.ref.Reference
object rx:
  // 一遍遍执行list中的元素函数，如果返回值为true，则删除掉
  private val runnableList = collection.mutable.Buffer[() => Boolean]()
  case class Lock()
  given Lock = Lock()
  Executors.newSingleThreadExecutor().nn.execute: () =>
    while (true)
      val index = runnableList.indexWhere(_())
      if index >= 0 then runnableList.remove(index) else Thread.`yield`()
  trait Rx[A]:
    def value(lock:Option[Lock]):A
    def addListener(lifeCycleTracking: Any)(l: (A, A) => Unit): Unit
    def map[B](f:A => B):Rx[B] = {
      val res = new Var[B](f(value(None)))
      addListener(res): (curr,next) =>
        if curr!=next then res.set0(f(next))(using None)
      res
    }
    def zip[B](rxb:Rx[B]):Rx[(A,B)] = flatMap(a => rxb.map(b => (a,b)))
    def flatMap[B](f:A => Rx[B]):Rx[B] = {
      val currentA = value(None)
      var currentRxb = f(currentA)
      val res = new Var[B](currentRxb.value(None))
      addListener(currentRxb): (curr,next) => 
        currentRxb = f(next)
        currentRxb.addListener(res):(curr,next) => 
          res.set0(next)(using None)
      res
    }
  case class Var[A](private var a:A) extends Rx[A]:
    val listeners = collection.mutable.Buffer[(A,A) => Unit]()
    override def value(using lockOption:Option[Lock]):A = lockOption match
      case None => a
      case Some(value) => value.synchronized(a)
    override def addListener(lifeCycleTracking: Any)(l: (A, A) => Unit): Unit =
      val queue = new ReferenceQueue[Any]()
      new PhantomReference(lifeCycleTracking,queue)
      listeners += l
      runnableList += {() => 
        queue.poll() match
          case null => false
          case any:Reference[?] => 
            listeners -= l
            true
      }
    def set(a:A)(using lock:Lock):Unit = set0(a)(using Some(lock))
    private[rx] def set0(a:A)(using lockOption:Option[Lock]):Unit = lockOption match
      case None =>
        if (this.a != a)
          val old =this.a
          this.a = a
          listeners.foreach(_(old,a))
      case Some(lock) => lock.synchronized:
        if (this.a != a)
          val old =this.a
          this.a = a
          listeners.foreach(_(old,a))
  type Closure[A] = A ?=> Unit
  given [A](using A):Left[A,Nothing] = Left(summon)
  given [A](using A):Right[Nothing,A] = Right(summon)
  sealed trait QEither[+A,+B,+C,+D]
  case class LL[A](a:A) extends QEither[A,Nothing,Nothing,Nothing]
  case class LR[A](a:A) extends QEither[Nothing,A,Nothing,Nothing]
  case class RL[A](a:A) extends QEither[Nothing,Nothing,A,Nothing]
  case class RR[A](a:A) extends QEither[Nothing,Nothing,Nothing,A]
  given [A](using A):LL[A] = LL(summon)
  given [A](using A):LR[A] = LR(summon)
  given [A](using A):RL[A] = RL(summon)
  given [A](using A):RR[A] = RR(summon)
  enum Scope(private[rx] val value: collection.mutable.HashMap[String, Any]):
    case Root(private[rx] override val value: collection.mutable.HashMap[String, Any]) extends Scope(value)
    case >>[+A <: Scope, +B](
        private[rx] override val value: collection.mutable.HashMap[String, Any]
    ) extends Scope(value)
  opaque type SingleValueKey[K, S <: Scope, V] = K
  opaque type MultiValueKey[K, S <: Scope, V] = K
  opaque type SingleNodeKey[K, S <: Scope, V] = K
  opaque type MultiNodeKey[K, S <: Scope, V] = K
  object Key:
    inline def singleValueKey[K <: String & Singleton, S <: Scope, V]: SingleValueKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
    inline def multiValueKey[K <: String & Singleton, S <: Scope, V]: MultiValueKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
    inline def singleNodeKey[K <: String & Singleton, S <: Scope, V]: SingleNodeKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
    inline def multiNodeKey[K <: String & Singleton, S <: Scope, V]: MultiNodeKey[K, S, V] =
      compiletime.constValue[K].asInstanceOf
  export Scope.*
  type GetResult[A,S,V] = A match
    case LL[?] => Option[S >> V]
    case LR[?] => Seq[S >> V]
    case RL[?] => Option[V]
    case RR[?] => Seq[V]
  extension [S <: Scope](a: S)
    def toHashMap: collection.immutable.HashMap[String, Any] = a.value.view
      .mapValues(_.asInstanceOf[Matchable])
      .mapValues {
        case x: Scope => x.toHashMap
        case iterable: collection.Iterable[?] =>
          iterable.map(_.asInstanceOf[Matchable]).map {
            case x: Scope => x.toHashMap
            case other    => other
          }
        case other => other
      }
      .to(collection.immutable.HashMap)
    def get[K <: String & Singleton,V](key:K)
    (using either:QEither[SingleNodeKey[K,S,V],MultiNodeKey[K,S,V],SingleValueKey[K,S,V],MultiValueKey[K,S,V]])
    :GetResult[either.type,S,V] = 
      (either match
        case LL(value) => a.value.get(key)
        case LR(value) => a.value.get(key) match
          case None        => Nil
          case Some(value) => value.asInstanceOf[collection.mutable.Buffer[S >> V]].toSeq
        case RL(value) => a.value.get(key)
        case RR(value) => a.value.get(key) match
          case None        => Nil
          case Some(value) => value.asInstanceOf[collection.mutable.Buffer[V]].toSeq
      ).asInstanceOf
  extension [K <: String & Singleton](key:K)
    def get[V,S <: Scope]
      (using scope:S,either:QEither[SingleNodeKey[K,S,V],MultiNodeKey[K,S,V],SingleValueKey[K,S,V],MultiValueKey[K,S,V]])
      :GetResult[either.type,S,V] =scope.get(key)(using either)
  import Scope.*
  def obj(closure:Closure[Root]):Rx[Root] = ???
  extension [S <: Scope, K <: Singleton & String, V](key: K)
    def :=(using s:S,e:Either[SingleValueKey[K,S,V],MultiValueKey[K,S,V]])(value: Rx[e.type match
      case Left[SingleValueKey[?,?,v],?] => v
      case Right[?,MultiValueKey[?,?,v]] => collection.Iterable[v]
    ]) = ???
    def +=(using S,MultiValueKey[K, S, V])(value: Rx[V]): Unit = ???
    def ++=(using S,MultiValueKey[K, S, V])(value: Rx[collection.Iterable[V]]): Unit = ???
    def apply(using s:S,either: Either[SingleNodeKey[K, S, V], MultiNodeKey[K, S, V]])(closure: Closure[S >> V]): Closure[S >> V] => Unit = ???
  def For[A <: Scope,B](using A)(iterable:Rx[Iterable[B]])(closure: B => Closure[A]):Rx[Boolean] = ???
  def If[A <: Scope](using A)(condition:Rx[Boolean])(closure:Closure[A]):Rx[Boolean] = ???
  extension [A <: Scope](condition:Rx[Boolean])
    def ElseIf(elseCondition:Rx[Boolean])(c0:Closure[A]):Rx[Boolean] = ???
    def Else(closure:Closure[A]):Unit = ???
object rxExample:
  @main def main = {
    import rx.{*,given}
    val x = Var(123)
    val y = x.map(_ +1)
    y.addListener(rxExample): (oldObj,newObj) => 
      println(s"old:$oldObj,new:$newObj")
    x.set(15)
    val a = obj:
      If(Var(true)){
        For(Var(Seq(1,2,3))){ a =>
          println(a)
        }
      }.ElseIf(Var(true)) {
      }.Else {

      }
  }